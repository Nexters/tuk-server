package nexters.tuk.application.scheduler

import nexters.tuk.application.scheduler.dto.request.GatheringCommand
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
    fun scheduleTukNotification(command: GatheringCommand.Notification) {
        val jobKey = JobKey(command.gatheringId.toString(), "notification-job-group")
        val triggerKey = TriggerKey(command.gatheringId.toString(), "notification-trigger-group")


        val jobDetail = JobBuilder.newJob(TukNotificationJob::class.java)
            .withIdentity(jobKey)
            .storeDurably()
            .requestRecovery(true)
            .build()

        val notificationTime = command.sendAt
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