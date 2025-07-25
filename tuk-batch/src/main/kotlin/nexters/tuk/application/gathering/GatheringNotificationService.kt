package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.notification.GatheringNotifier
import nexters.tuk.application.scheduler.GatheringNotificationScheduler
import org.springframework.stereotype.Service

@Service
class GatheringNotificationService(
    private val gatheringNotifier: GatheringNotifier,
    private val gatheringNotificationScheduler: GatheringNotificationScheduler
) {
    fun handleGatheringNotification(command: GatheringCommand.Notification) {
        when (command) {
            is GatheringCommand.Notification.Tuk -> {
                gatheringNotificationScheduler.scheduleTukNotification(command)
            }

            is GatheringCommand.Notification.Invitation -> {
                gatheringNotifier.sendInvitationNotification(command)
            }
        }
    }
}