package nexters.tuk.application.scheduler.dto.request

import java.time.LocalDateTime

class GatheringCommand {
    data class Notification(
        val gatheringId: String,
        val sendAt: LocalDateTime,
    )
}