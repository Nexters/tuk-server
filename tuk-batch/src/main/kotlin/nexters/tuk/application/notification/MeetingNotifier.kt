package nexters.tuk.application.notification

import nexters.tuk.application.meeting.dto.request.MeetingCommand
import nexters.tuk.application.member.MemberService
import org.springframework.stereotype.Service


@Service
class MeetingNotifier(
    private val memberService: MemberService,
    private val notificationSender: NotificationSender
) {
    fun sendTukNotification(command: MeetingCommand.Notification.Tuk) {
        val tokens = memberService.findTokensByMeetingId(command.meetingId)
        val tukMessage = TukNotificationMessage(command.meetingId, command.intervalDays)

        notificationSender.notifyMembers(tokens, tukMessage)
    }

    fun sendInvitationNotification(command: MeetingCommand.Notification.Invitation) {
        val tokens = memberService.findTokensByMeetingId(command.meetingId)
        val invitationMessage = InvitationNotificationMessage(command.meetingId, command.purpose)

        notificationSender.notifyMembers(tokens, invitationMessage)
    }
}