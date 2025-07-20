package nexters.tuk.application.meeting

class MeetingCommand {
    data class SendInvitation(
        val meetingId: Long,
        val purpose: String,
    )

    data class SendTuk(
        val meetingId: Long,
        val durationDays: Long
    )
}