package nexters.tuk.application.gathering.vo

import com.fasterxml.jackson.annotation.JsonValue
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@JvmInline
value class RelativeTime private constructor(@get:JsonValue val value: String) {
    companion object {
        fun from(dateTime: LocalDateTime): RelativeTime {
            val now = LocalDateTime.now()
            val minutes = ChronoUnit.MINUTES.between(dateTime, now)
            val hours = ChronoUnit.HOURS.between(dateTime, now)
            val days = ChronoUnit.DAYS.between(dateTime, now)
            val weeks = days / 7
            val months = ChronoUnit.MONTHS.between(dateTime, now)
            val years = ChronoUnit.YEARS.between(dateTime, now)

            return when {
                minutes < 1 -> RelativeTime("방금 전")
                minutes < 60 -> RelativeTime("${minutes}분 전")
                hours < 24 -> RelativeTime("${hours}시간 전")
                days == 0L -> RelativeTime("오늘")
                days < 7 -> RelativeTime("${days}일 전")
                days < 30 -> RelativeTime("${weeks}주 전")
                months < 12 -> RelativeTime("${months}개월 전")
                else -> RelativeTime("${years}년 전")
            }
        }

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