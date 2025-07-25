package nexters.tuk.application.scheduler

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.job.TukNotificationJob
import org.quartz.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class GatheringNotificationScheduler(
    private val scheduler: Scheduler,
) {
    fun scheduleTukNotification(command: GatheringCommand.Notification.Tuk) {
        val jobKey = JobKey(command.gatheringId.toString(), "notification-job-group")
        val triggerKey = TriggerKey(command.gatheringId.toString(), "notification-trigger-group")


        val jobDataMap = JobDataMap(
            mapOf(
                "gatheringId" to command.gatheringId,
                "intervalDays" to command.intervalDays
            )
        )

        val jobDetail = JobBuilder.newJob(TukNotificationJob::class.java)
            .withIdentity(jobKey)
            .usingJobData(jobDataMap)
            .storeDurably()
            .requestRecovery(true)
            .build()

        val notificationTime = LocalDateTime.now().plusDays(command.intervalDays)
        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startAt(notificationTime.toDate())
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
            .forJob(jobKey)
            .build()

        scheduler.scheduleJob(jobDetail, setOf(trigger), true)
    }

    private fun LocalDateTime.toDate(): Date {
        val instant = this.atZone(ZoneId.systemDefault()).toInstant()
        return Date.from(instant)
    }
}