package nexters.tuk.application.meeting.dto.request


class MeetingCommand {
    data class Generate(
        val memberId: Long,
        val meetingName: String,
        val daysSinceLastMeeting: Long,
        val meetingIntervalDays: Long,
        val tags: List<String>,
    )

    data class GetMemberMeetings(
        val memberId: Long,
    )
}