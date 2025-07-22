package nexters.tuk.application.notification

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.member.MemberService
import org.springframework.stereotype.Service


@Service
class GatheringNotifier(
    private val memberService: MemberService,
    private val notificationSender: NotificationSender
) {
    fun sendTukNotification(command: GatheringCommand.Notification.Tuk) {
        val tokens = memberService.findTokensByGatheringId(command.gatheringId)
        val tukMessage = TukNotificationMessage(command.gatheringId, command.intervalDays)

        notificationSender.notifyMembers(tokens, tukMessage)
    }

    fun sendInvitationNotification(command: GatheringCommand.Notification.Invitation) {
        val tokens = memberService.findTokensByGatheringId(command.gatheringId)
        val invitationMessage = InvitationNotificationMessage(command.gatheringId, command.purpose)

        notificationSender.notifyMembers(tokens, invitationMessage)
    }
}