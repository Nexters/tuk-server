package nexters.tuk.application.notification

import nexters.tuk.application.scheduler.MeetingScheduler
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NotificationService(
    private val notificationSender: NotificationSender,
    private val meetingScheduler: MeetingScheduler
) {
    fun sendNotification(meetingId: Long, notificationType: NotificationType, notificationTime: LocalDateTime) {
        when(notificationType) {
            NotificationType.RECURRING -> meetingScheduler.scheduleNotification(meetingId, notificationTime)
            NotificationType.TUK -> notificationSender.sendTukNotification(meetingId)
        }
    }
}