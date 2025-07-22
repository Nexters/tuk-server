package nexters.tuk.application.meeting.dto.request

class MeetingCommand {
    sealed class Notification {
        data class Tuk(val meetingId: Long, val intervalDays: Long) : Notification()
        data class Invitation(val meetingId: Long, val purpose: String) : Notification()
    }
}