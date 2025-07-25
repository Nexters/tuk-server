package nexters.tuk.application.gathering.dto.request

class GatheringCommand {
    sealed class Notification {
        data class Tuk(val gatheringId: Long, val intervalDays: Long) : Notification()
        data class Invitation(val gatheringId: Long, val purpose: String) : Notification()
    }
}