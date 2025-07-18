package nexters.tuk.application.meeting

interface MeetingService {
    fun getDaysSinceLastMeeting(meetingId: Long): Int
}