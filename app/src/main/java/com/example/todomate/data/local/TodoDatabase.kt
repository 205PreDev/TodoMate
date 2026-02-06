package com.example.todomate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TodoEntity::class, LifeAreaEntity::class, WeeklyGoalEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao
    abstract fun lifeAreaDao(): LifeAreaDao
    abstract fun weeklyGoalDao(): WeeklyGoalDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // life_areas 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS life_areas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL DEFAULT '',
                        color INTEGER NOT NULL DEFAULT 0,
                        isDefault INTEGER NOT NULL DEFAULT 0,
                        orderIndex INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // weekly_goals 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS weekly_goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        lifeAreaId INTEGER NOT NULL,
                        weekStartDate INTEGER NOT NULL,
                        targetPercentage INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY (lifeAreaId) REFERENCES life_areas(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_weekly_goals_lifeAreaId ON weekly_goals(lifeAreaId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_weekly_goals_weekStartDate ON weekly_goals(weekStartDate)")

                // todos 테이블에 lifeAreaId 컬럼 추가
                db.execSQL("ALTER TABLE todos ADD COLUMN lifeAreaId INTEGER DEFAULT NULL REFERENCES life_areas(id) ON DELETE SET NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_todos_lifeAreaId ON todos(lifeAreaId)")

                // 기본 생활 영역 삽입
                db.execSQL("INSERT INTO life_areas (id, name, icon, isDefault, orderIndex) VALUES (1, '커리어', 'work', 1, 0)")
                db.execSQL("INSERT INTO life_areas (id, name, icon, isDefault, orderIndex) VALUES (2, '건강', 'health', 1, 1)")
                db.execSQL("INSERT INTO life_areas (id, name, icon, isDefault, orderIndex) VALUES (3, '학습', 'study', 1, 2)")
                db.execSQL("INSERT INTO life_areas (id, name, icon, isDefault, orderIndex) VALUES (4, '관계', 'relationship', 1, 3)")
                db.execSQL("INSERT INTO life_areas (id, name, icon, isDefault, orderIndex) VALUES (5, '재정', 'finance', 1, 4)")
            }
        }

        fun getInstance(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 새 설치 시 기본 생활 영역 삽입
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.lifeAreaDao()?.insertAll(LifeAreaEntity.DEFAULT_AREAS)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
