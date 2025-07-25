package nexters.tuk.job

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.notification.GatheringNotifier
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class TukNotificationJob(
    private val gatheringNotifier: GatheringNotifier
) : Job {
    override fun execute(context: JobExecutionContext) {
        val jobDataMap = context.mergedJobDataMap
        val gatheringId = jobDataMap["gatheringId"] as Long
        val intervalDays = jobDataMap["intervalDays"] as Long

        val command = GatheringCommand.Notification.Tuk(gatheringId, intervalDays)
        gatheringNotifier.sendTukNotification(command)
    }
}