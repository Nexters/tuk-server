package nexters.tuk.application.scheduler.dto

import java.time.LocalDateTime

class NotificationCommand {
    data class Reservation(
        val meetingId: Long,
        val notificationTime: LocalDateTime
    )
}