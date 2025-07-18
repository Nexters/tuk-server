package nexters.tuk.application.scheduler

import nexters.tuk.application.notification.NotificationSender
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class SendMeetingNotificationJob(
    private val notificationSender: NotificationSender,
) : Job {
    override fun execute(context: JobExecutionContext) {
        val jobDataMap = context.mergedJobDataMap
        val meetingId = jobDataMap["meetingId"] as Long
        notificationSender.sendRecurringNotification(meetingId)
    }
}