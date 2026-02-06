package com.example.todomate.util

import java.util.Calendar

object DateUtils {

    /**
     * 주어진 timestamp가 속한 주의 시작일(월요일) 00:00:00 반환
     */
    fun getWeekStartDate(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 월요일로 이동
            val dayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        }
        return calendar.timeInMillis
    }

    /**
     * 주어진 timestamp가 속한 주의 종료일(일요일) 23:59:59 반환
     */
    fun getWeekEndDate(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = getWeekStartDate(timestamp)
            add(Calendar.DAY_OF_MONTH, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    /**
     * 주 시작일을 "MM/dd ~ MM/dd" 형식으로 반환
     */
    fun formatWeekRange(weekStartDate: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = weekStartDate
        }
        val startMonth = calendar.get(Calendar.MONTH) + 1
        val startDay = calendar.get(Calendar.DAY_OF_MONTH)

        calendar.add(Calendar.DAY_OF_MONTH, 6)
        val endMonth = calendar.get(Calendar.MONTH) + 1
        val endDay = calendar.get(Calendar.DAY_OF_MONTH)

        return "$startMonth/$startDay ~ $endMonth/$endDay"
    }

    /**
     * 이전 주 시작일 반환
     */
    fun getPreviousWeekStartDate(currentWeekStartDate: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentWeekStartDate
            add(Calendar.WEEK_OF_YEAR, -1)
        }
        return calendar.timeInMillis
    }

    /**
     * 다음 주 시작일 반환
     */
    fun getNextWeekStartDate(currentWeekStartDate: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentWeekStartDate
            add(Calendar.WEEK_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
