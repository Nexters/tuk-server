package nexters.tuk.job

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class TukNotificationJob(
) : Job {
    override fun execute(context: JobExecutionContext) {
        TODO("알림 API 전송 구현")
    }
}