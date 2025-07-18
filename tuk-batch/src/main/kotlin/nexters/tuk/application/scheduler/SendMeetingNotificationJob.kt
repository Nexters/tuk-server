package nexters.tuk.application.scheduler

import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component

@Component
class SendMeetingNotificationJob : Job {

    override fun execute(context: JobExecutionContext) {
        val jobDataMap = context.mergedJobDataMap
        val meetingId = jobDataMap["meetingId"] as Long
        TODO("모임내 사용자에게 알림 보내는 로직 구현")
    }
}