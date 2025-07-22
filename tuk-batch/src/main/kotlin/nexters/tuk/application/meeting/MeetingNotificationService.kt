package nexters.tuk.application.meeting

import nexters.tuk.application.meeting.dto.request.MeetingCommand
import nexters.tuk.application.notification.MeetingNotifier
import nexters.tuk.application.scheduler.MeetingNotificationScheduler
import org.springframework.stereotype.Service

@Service
class MeetingNotificationService(
    private val meetingNotifier: MeetingNotifier,
    private val meetingNotificationScheduler: MeetingNotificationScheduler
) {
    fun handleMeetingNotification(command: MeetingCommand.Notification) {
        when (command) {
            is MeetingCommand.Notification.Tuk -> {
                meetingNotificationScheduler.scheduleTukNotification(command)
            }

            is MeetingCommand.Notification.Invitation -> {
                meetingNotifier.sendInvitationNotification(command)
            }
        }
    }
}