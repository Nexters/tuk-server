package nexters.tuk.application.scheduler

import nexters.tuk.application.meeting.dto.request.MeetingCommand
import nexters.tuk.job.TukNotificationJob
import org.quartz.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class MeetingNotificationScheduler(
    private val scheduler: Scheduler,
) {
    fun scheduleTukNotification(command: MeetingCommand.Notification.Tuk) {
        val jobKey = JobKey(command.meetingId.toString(), "notification-job-group")
        val triggerKey = TriggerKey(command.meetingId.toString(), "notification-trigger-group")


        val jobDataMap = JobDataMap(
            mapOf(
                "meetingId" to command.meetingId,
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