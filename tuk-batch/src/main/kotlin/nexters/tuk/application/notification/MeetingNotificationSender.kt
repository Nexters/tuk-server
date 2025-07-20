package nexters.tuk.application.notification

import nexters.tuk.application.member.MemberService
import nexters.tuk.infrastructure.FcmClient
import org.springframework.stereotype.Component


@Component
class MeetingNotificationSender(
    private val memberService: MemberService,
    private val fcmClient: FcmClient
) {
    fun sendNotification(meetingId: Long, messageGenerator: MessageGenerator) {
        val tokens = memberService.findTokensByMeetingId(meetingId)
        fcmClient.sendMulticast(tokens, messageGenerator.getTitle(), messageGenerator.getBody())
    }
}