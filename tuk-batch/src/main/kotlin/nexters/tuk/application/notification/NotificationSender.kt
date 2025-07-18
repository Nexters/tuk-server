package nexters.tuk.application.notification

import nexters.tuk.application.meeting.MeetingService
import nexters.tuk.application.member.MemberService
import nexters.tuk.infrastructure.FcmClient
import org.springframework.stereotype.Component

@Component
class NotificationSender(
    private val memberService: MemberService,
    private val meetingService: MeetingService,
    private val fcmClient: FcmClient
) {
    fun sendTukNotification(meetingId: Long) {
        val tokens = memberService.findTokensByMeetingId(meetingId)
        TODO("title과 body 만들고 fcmClient를 통해서 데이터 전송")
    }

    fun sendRecurringNotification(meetingId: Long) {
        val tokens = memberService.findTokensByMeetingId(meetingId)
        val days = meetingService.getDaysSinceLastMeeting(meetingId)
        TODO("title과 body 만들고 fcmClient를 통해서 데이터 전송")
    }
}