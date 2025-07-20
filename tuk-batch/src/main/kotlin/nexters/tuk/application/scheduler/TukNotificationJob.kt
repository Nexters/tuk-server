package nexters.tuk.application.scheduler

import nexters.tuk.application.notification.MeetingNotificationSender
import nexters.tuk.application.notification.TukMessageGenerator
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class TukNotificationJob(
    private val meetingNotificationSender: MeetingNotificationSender
) : Job {
    override fun execute(context: JobExecutionContext) {
        val jobDataMap = context.mergedJobDataMap
        val meetingId = jobDataMap["meetingId"] as Long
        val durationDays = jobDataMap["durationDays"] as Long

        val generator = TukMessageGenerator(meetingId, durationDays)
        meetingNotificationSender.sendNotification(meetingId, generator)
    }
}