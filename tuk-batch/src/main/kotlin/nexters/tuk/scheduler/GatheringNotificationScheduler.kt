package nexters.tuk.scheduler

import jakarta.annotation.PostConstruct
import nexters.tuk.job.TukJob
import org.quartz.Scheduler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GatheringNotificationScheduler(
    private val scheduler: Scheduler,
    private val jobs: List<TukJob>,
) {
    private val logger = LoggerFactory.getLogger(GatheringNotificationScheduler::class.java)

    @PostConstruct
    fun gatheringJobScheduler() {
        logger.info("[GatheringNotificationScheduler] 스케줄러 초기화 시작")

        jobs.forEach { job ->
            if (scheduler.checkExists(job.getJobDetail().key)) {
                scheduler.deleteJob(job.getJobDetail().key)
            }
            scheduler.scheduleJob(job.getJobDetail(), setOf(job.getTrigger()), true)
        }

        scheduler.start()
        logger.info("[GatheringNotificationScheduler] 스케줄러 초기화 완료 - 10분마다 실행")
    }
}