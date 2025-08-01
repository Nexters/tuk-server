package nexters.tuk.scheduler

import jakarta.annotation.PostConstruct
import nexters.tuk.job.GatheringPushCheckJob
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GatheringNotificationScheduler(
    private val scheduler: Scheduler,
) {
    private val logger = LoggerFactory.getLogger(GatheringNotificationScheduler::class.java)

    @PostConstruct
    fun gatheringJobScheduler() {
        logger.info("[GatheringNotificationScheduler] 스케줄러 초기화 시작")
        val jobKey = JobKey("gathering-check-job", "gathering-check-group")
        val triggerKey = TriggerKey("gathering-check-trigger", "gathering-check-group")

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey)
        }

        val jobDetail = JobBuilder.newJob(GatheringPushCheckJob::class.java)
            .withIdentity(jobKey)
            .storeDurably()
            .requestRecovery(true)
            .build()

        // 10분마다 실행하는 cron
        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNow()
            .withSchedule(CronScheduleBuilder.cronSchedule("0 */10 * * * ?"))
            .forJob(jobKey)
            .build()

        scheduler.scheduleJob(jobDetail, setOf(trigger), true)
        logger.info("[GatheringNotificationScheduler] 스케줄러 초기화 완료 - 10분마다 실행")
    }
}