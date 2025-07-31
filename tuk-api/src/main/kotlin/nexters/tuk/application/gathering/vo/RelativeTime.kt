package nexters.tuk.application.gathering.vo

import com.fasterxml.jackson.annotation.JsonValue

@JvmInline
value class RelativeTime private constructor(@get:JsonValue val value: String) {
    companion object {
        fun fromDays(days: Int): RelativeTime {
            val daysInWeek = 7
            val daysInMonth = 30
            val daysInYear = 365

            return when {
                days == 0 -> RelativeTime("오늘")
                days < daysInWeek -> RelativeTime("${days}일 전")
                days < daysInMonth -> RelativeTime("${days / daysInWeek}주 전")
                days < daysInYear -> RelativeTime("${days / daysInMonth}개월 전")
                else -> RelativeTime("${days / daysInYear}년 전")
            }
        }
    }
}