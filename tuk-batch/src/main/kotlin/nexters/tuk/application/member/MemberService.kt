package nexters.tuk.application.member


interface MemberService {
    fun findTokensByMeetingId(meetingId: Long): List<String>
}