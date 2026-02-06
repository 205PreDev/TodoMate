package com.example.todomate.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.todomate.ai.EncouragementResult
import com.example.todomate.ai.GeminiService
import com.example.todomate.data.local.LifeAreaCount
import com.example.todomate.data.local.LifeAreaEntity
import com.example.todomate.data.local.TodoDatabase
import com.example.todomate.data.local.WeeklyGoalEntity
import com.example.todomate.data.repository.DashboardRepository
import com.example.todomate.data.repository.TodoRepository
import com.example.todomate.util.DateUtils
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TodoDatabase.getInstance(application)
    private val todoRepository = TodoRepository(database.todoDao())
    private val dashboardRepository = DashboardRepository(
        database.lifeAreaDao(),
        database.weeklyGoalDao()
    )
    private val geminiService = GeminiService()

    // 현재 선택된 주의 시작일
    private val _currentWeekStartDate = MutableLiveData(DateUtils.getWeekStartDate())
    val currentWeekStartDate: LiveData<Long> = _currentWeekStartDate

    // 생활 영역 목록
    val lifeAreas: LiveData<List<LifeAreaEntity>> = dashboardRepository.getAllLifeAreas()

    // 현재 주의 목표
    val weeklyGoals: LiveData<List<WeeklyGoalEntity>> = _currentWeekStartDate.switchMap { weekStart ->
        dashboardRepository.getGoalsForWeek(weekStart)
    }

    // 현재 주의 할 일 분포 (전체)
    val todoDistribution: LiveData<List<LifeAreaCount>> = _currentWeekStartDate.switchMap { weekStart ->
        val weekEnd = DateUtils.getWeekEndDate(weekStart)
        todoRepository.getTodoCountByLifeArea(weekStart, weekEnd)
    }

    // 현재 주의 완료된 할 일 분포
    val completedDistribution: LiveData<List<LifeAreaCount>> = _currentWeekStartDate.switchMap { weekStart ->
        val weekEnd = DateUtils.getWeekEndDate(weekStart)
        todoRepository.getCompletedCountByLifeArea(weekStart, weekEnd)
    }

    // AI 격려 메시지
    private val _encouragementMessage = MutableLiveData<AiMessageState>()
    val encouragementMessage: LiveData<AiMessageState> = _encouragementMessage

    // 레이더 차트 데이터 (목표 vs 실제)
    data class RadarChartData(
        val labels: List<String>,
        val goalValues: List<Float>,
        val actualValues: List<Float>
    )

    sealed class AiMessageState {
        object Loading : AiMessageState()
        data class Success(val message: String) : AiMessageState()
        data class Error(val message: String) : AiMessageState()
    }

    private val _radarChartData = MediatorLiveData<RadarChartData>()
    val radarChartData: LiveData<RadarChartData> = _radarChartData

    init {
        // 모든 데이터 소스를 결합하여 레이더 차트 데이터 생성
        _radarChartData.addSource(lifeAreas) { updateRadarChartData() }
        _radarChartData.addSource(weeklyGoals) { updateRadarChartData() }
        _radarChartData.addSource(todoDistribution) { updateRadarChartData() }
    }

    private fun updateRadarChartData() {
        val areas = lifeAreas.value ?: return
        val goals = weeklyGoals.value ?: emptyList()
        val todos = todoDistribution.value ?: emptyList()

        if (areas.isEmpty()) return

        val labels = areas.map { it.name }
        val goalValues = areas.map { area ->
            goals.find { it.lifeAreaId == area.id }?.targetPercentage?.toFloat() ?: 0f
        }

        // 실제 할 일 분포를 백분율로 계산
        val totalTodos = todos.sumOf { it.count }
        val actualValues = areas.map { area ->
            if (totalTodos == 0) 0f
            else {
                val count = todos.find { it.lifeAreaId == area.id }?.count ?: 0
                (count.toFloat() / totalTodos * 100)
            }
        }

        _radarChartData.value = RadarChartData(labels, goalValues, actualValues)
    }

    /**
     * AI 격려 메시지 요청
     */
    fun requestEncouragement() {
        val areas = lifeAreas.value ?: return
        val todos = todoDistribution.value ?: emptyList()
        val completed = completedDistribution.value ?: emptyList()
        val goals = weeklyGoals.value ?: emptyList()

        if (areas.isEmpty()) {
            _encouragementMessage.value = AiMessageState.Error("생활 영역을 먼저 설정해주세요")
            return
        }

        _encouragementMessage.value = AiMessageState.Loading

        viewModelScope.launch {
            // 영역별 통계 생성
            val weeklyStats = mutableMapOf<String, Int>()
            val completedStats = mutableMapOf<String, Int>()
            val goalStats = mutableMapOf<String, Int>()

            areas.forEach { area ->
                val areaName = area.name
                weeklyStats[areaName] = todos.find { it.lifeAreaId == area.id }?.count ?: 0
                completedStats[areaName] = completed.find { it.lifeAreaId == area.id }?.count ?: 0
                goalStats[areaName] = goals.find { it.lifeAreaId == area.id }?.targetPercentage ?: 0
            }

            when (val result = geminiService.generateEncouragement(weeklyStats, completedStats, goalStats)) {
                is EncouragementResult.Success -> {
                    _encouragementMessage.value = AiMessageState.Success(result.message)
                }
                is EncouragementResult.Error -> {
                    _encouragementMessage.value = AiMessageState.Error(result.message)
                }
            }
        }
    }

    /**
     * 기본 격려 메시지 (AI 실패 시 또는 데이터 없을 때)
     */
    fun getDefaultEncouragement(): String {
        val todos = todoDistribution.value ?: emptyList()
        val completed = completedDistribution.value ?: emptyList()

        val totalTodos = todos.sumOf { it.count }
        val totalCompleted = completed.sumOf { it.count }

        return when {
            totalTodos == 0 -> "📝 이번 주 할 일을 추가하고 목표를 향해 나아가보세요!"
            totalCompleted == 0 -> "💪 할 일이 준비되었어요. 하나씩 완료해볼까요?"
            totalCompleted == totalTodos -> "🎉 이번 주 할 일을 모두 완료했어요! 대단해요!"
            totalCompleted > totalTodos / 2 -> "👍 잘 진행하고 있어요! 조금만 더 힘내세요!"
            else -> "🌱 천천히 하나씩 해나가면 돼요. 화이팅!"
        }
    }

    fun navigateToPreviousWeek() {
        _currentWeekStartDate.value?.let { current ->
            _currentWeekStartDate.value = DateUtils.getPreviousWeekStartDate(current)
        }
    }

    fun navigateToNextWeek() {
        _currentWeekStartDate.value?.let { current ->
            val nextWeek = DateUtils.getNextWeekStartDate(current)
            // 미래 주로는 이동하지 않음
            if (nextWeek <= DateUtils.getWeekStartDate()) {
                _currentWeekStartDate.value = nextWeek
            }
        }
    }

    fun saveWeeklyGoals(goals: Map<Long, Int>) {
        viewModelScope.launch {
            val weekStart = _currentWeekStartDate.value ?: return@launch
            val goalEntities = goals.map { (areaId, percentage) ->
                WeeklyGoalEntity(
                    lifeAreaId = areaId,
                    weekStartDate = weekStart,
                    targetPercentage = percentage
                )
            }
            dashboardRepository.saveGoals(goalEntities)
        }
    }

    fun addCustomLifeArea(name: String) {
        viewModelScope.launch {
            val areas = dashboardRepository.getAllLifeAreasSync()
            val maxOrder = areas.maxOfOrNull { it.orderIndex } ?: 0
            dashboardRepository.insertLifeArea(
                LifeAreaEntity(
                    name = name,
                    isDefault = false,
                    orderIndex = maxOrder + 1
                )
            )
        }
    }

    fun deleteLifeArea(lifeArea: LifeAreaEntity) {
        viewModelScope.launch {
            dashboardRepository.deleteLifeArea(lifeArea)
        }
    }

    fun resetLifeAreas() {
        viewModelScope.launch {
            // 모든 기존 영역 삭제
            dashboardRepository.deleteAllLifeAreas()
            // 기본 영역 다시 삽입
            dashboardRepository.insertLifeAreasReplace(LifeAreaEntity.DEFAULT_AREAS)
        }
    }

    fun getWeekRangeText(): String {
        return _currentWeekStartDate.value?.let { DateUtils.formatWeekRange(it) } ?: ""
    }

    fun isCurrentWeek(): Boolean {
        return _currentWeekStartDate.value == DateUtils.getWeekStartDate()
    }
}
