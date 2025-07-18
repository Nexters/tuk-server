package nexters.tuk.application

import io.mockk.spyk
import io.mockk.verify
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.scheduler.MeetingScheduler
import nexters.tuk.application.scheduler.SendMeetingNotificationJob
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@SpringBootTest
class QuartzSchedulerTest @Autowired constructor(
    private val scheduler: Scheduler,
    private val meetingScheduler: MeetingScheduler,
    private val realJob: SendMeetingNotificationJob,
) {

    private lateinit var spyJob: SendMeetingNotificationJob

    @BeforeEach
    fun setup() {
        spyJob = spyk(realJob)
    }

    @AfterEach
    fun tearDown() {
        scheduler.clear()
    }

    @Test
    fun `새로운 meetingId로 알림을 등록하면 Job과 Trigger가 생성된다`() {
        val meetingId = 1L
        val sendAt = LocalDateTime.now().plusHours(1)

        val expectedJobKey = JobKey(meetingId.toString(), "notification-job-group")
        val expectedTriggerKey = TriggerKey(meetingId.toString(), "notification-trigger-group")

        meetingScheduler.scheduleNotification(meetingId, sendAt)

        with(scheduler) {
            val jobDetail = getJobDetail(expectedJobKey)
            assertThat(jobDetail).isNotNull
            assertThat(jobDetail.key).isEqualTo(expectedJobKey)

            val triggers = getTriggersOfJob(expectedJobKey)
            assertThat(triggers).hasSize(1)
            assertThat(triggers.first().key).isEqualTo(expectedTriggerKey)
        }
    }

    @Test
    fun `동일 meetingId로 재등록 시 기존 Trigger가 새 정보로 교체된다`() {
        val meetingId = 2L
        val initialTime = LocalDateTime.now().plusHours(1)
        val updatedTime = LocalDateTime.now().plusHours(2)

        meetingScheduler.scheduleNotification(meetingId, initialTime)
        meetingScheduler.scheduleNotification(meetingId, updatedTime)

        val jobKey = JobKey(meetingId.toString(), "notification-job-group")
        val jobDetail = scheduler.getJobDetail(jobKey)
        assertThat(jobDetail).isNotNull
        assertThat(jobDetail.jobDataMap["meetingId"]).isEqualTo(meetingId)

        val triggers = scheduler.getTriggersOfJob(jobKey)
        assertThat(triggers).hasSize(1)

        val expectedTime = Date.from(updatedTime.atZone(ZoneId.systemDefault()).toInstant())
        assertThat(triggers.first().startTime).isEqualToIgnoringMillis(expectedTime)
    }

    @Test
    fun `여러 meetingId로 등록 시 각 스케줄이 독립적으로 존재한다`() {

        meetingScheduler.scheduleNotification(10L, LocalDateTime.now().plusMinutes(10))
        meetingScheduler.scheduleNotification(20L, LocalDateTime.now().plusMinutes(20))

        val jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals("notification-job-group"))
        assertThat(jobKeys).hasSize(2)
        assertThat(jobKeys).contains(
            JobKey("10", "notification-job-group"),
            JobKey("20", "notification-job-group")
        )
    }

    @Test
    @Disabled("SendMeetingNotificationJob의 TODO()로 인한 예외발생")
    fun `스케줄된 Job이 지정된 시간에 실행된다`() {
        val meetingId = 3L
        val sendAt = LocalDateTime.now().plusSeconds(1)

        meetingScheduler.scheduleNotification(meetingId, sendAt)

        Thread.sleep(3000)
        verify(timeout = 3000) { spyJob.execute(any()) }
    }
}