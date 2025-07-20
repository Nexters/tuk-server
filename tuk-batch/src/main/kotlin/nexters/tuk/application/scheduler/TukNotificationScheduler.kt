package nexters.tuk.application.scheduler

import org.quartz.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class TukNotificationScheduler(
    private val scheduler: Scheduler,
) {
    fun scheduleNotification(meetingId: Long, durationDays: Long) {
        val jobKey = JobKey(meetingId.toString(), "notification-job-group")
        val triggerKey = TriggerKey(meetingId.toString(), "notification-trigger-group")


        val jobDataMap = JobDataMap(
            mapOf(
                "meetingId" to meetingId,
                "durationDays" to durationDays
            )
        )

        val jobDetail = JobBuilder.newJob(TukNotificationJob::class.java)
            .withIdentity(jobKey)
            .usingJobData(jobDataMap)
            .storeDurably()
            .requestRecovery(true)
            .build()

        val notificationTime = LocalDateTime.now().plusDays(durationDays)
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