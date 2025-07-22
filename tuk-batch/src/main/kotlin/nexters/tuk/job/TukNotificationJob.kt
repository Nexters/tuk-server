package nexters.tuk.job

import nexters.tuk.application.meeting.dto.request.MeetingCommand
import nexters.tuk.application.notification.MeetingNotifier
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class TukNotificationJob(
    private val meetingNotifier: MeetingNotifier
) : Job {
    override fun execute(context: JobExecutionContext) {
        val jobDataMap = context.mergedJobDataMap
        val meetingId = jobDataMap["meetingId"] as Long
        val intervalDays = jobDataMap["intervalDays"] as Long

        val command = MeetingCommand.Notification.Tuk(meetingId, intervalDays)
        meetingNotifier.sendTukNotification(command)
    }
}