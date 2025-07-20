package nexters.tuk.application.meeting

import nexters.tuk.application.notification.InvitationMessageGenerator
import nexters.tuk.application.notification.MeetingNotificationSender
import nexters.tuk.application.scheduler.TukNotificationScheduler
import org.springframework.stereotype.Service

@Service
class MeetingService(
    private val tukNotificationScheduler: TukNotificationScheduler,
    private val meetingNotificationSender: MeetingNotificationSender
) {
    fun sendInvitation(command: MeetingCommand.SendInvitation) {
        val generator = InvitationMessageGenerator(command.meetingId, command.purpose)
        meetingNotificationSender.sendNotification(command.meetingId, generator)
    }

    fun scheduleTuk(command: MeetingCommand.SendTuk) {
        tukNotificationScheduler.scheduleNotification(command.meetingId, command.durationDays)
    }
}