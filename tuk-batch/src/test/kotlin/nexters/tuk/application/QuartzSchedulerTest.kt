package nexters.tuk.application

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.scheduler.TukNotificationJob
import nexters.tuk.application.scheduler.TukNotificationScheduler
import nexters.tuk.config.FcmConfig
import nexters.tuk.infrastructure.FcmClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.ZoneId

@SpringBootTest
@ActiveProfiles("test")
class QuartzSchedulerTest @Autowired constructor(
    private val scheduler: Scheduler,
    private val tukNotificationScheduler: TukNotificationScheduler,
    private val realTukNotificationJob: TukNotificationJob,
    @MockkBean private val memberService: MemberService,
    @MockkBean private val fcmClient: FcmClient,
    @MockkBean private val fcmConfig: FcmConfig,
) {

    private lateinit var spyTukNotificationJob: TukNotificationJob

    @BeforeEach
    fun setup() {
        spyTukNotificationJob = spyk(realTukNotificationJob)
        scheduler.setJobFactory { _, _ -> spyTukNotificationJob }
    }

    @AfterEach
    fun tearDown() {
        scheduler.clear()
    }

    @Test
    fun `새로운 미팅 ID로 알림을 등록하면 Job과 Trigger가 생성된다`() {
        // given
        val meetingId = 1L
        val durationDays = 1L
        val expectedJobKey = JobKey(meetingId.toString(), "notification-job-group")
        val expectedTriggerKey = TriggerKey(meetingId.toString(), "notification-trigger-group")

        // when
        tukNotificationScheduler.scheduleNotification(meetingId, durationDays)

        // then
        val jobDetail = scheduler.getJobDetail(expectedJobKey)
        assertThat(jobDetail).isNotNull
        assertThat(jobDetail.key).isEqualTo(expectedJobKey)

        val triggers = scheduler.getTriggersOfJob(expectedJobKey)
        assertThat(triggers).hasSize(1)
        assertThat(triggers.first().key).isEqualTo(expectedTriggerKey)
    }

    @Test
    fun `동일한 미팅 ID로 재등록 시 기존 Trigger가 새 정보로 교체된다`() {
        // given
        val meetingId = 2L
        val initialDurationDays = 1L
        val updatedDurationDays = 2L

        // when
        tukNotificationScheduler.scheduleNotification(meetingId, initialDurationDays)
        tukNotificationScheduler.scheduleNotification(meetingId, updatedDurationDays)

        // then
        val jobKey = JobKey(meetingId.toString(), "notification-job-group")
        val jobDetail = scheduler.getJobDetail(jobKey)
        assertThat(jobDetail).isNotNull
        assertThat(jobDetail.jobDataMap["meetingId"]).isEqualTo(meetingId)
        assertThat(jobDetail.jobDataMap["durationDays"]).isEqualTo(updatedDurationDays)

        val triggers = scheduler.getTriggersOfJob(jobKey)
        assertThat(triggers).hasSize(1)

        val expectedTime = LocalDateTime.now().plusDays(updatedDurationDays)
        val triggerTime = triggers.first().startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

        assertThat(triggerTime).isCloseTo(
            expectedTime,
            org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.SECONDS)
        )
    }

    @Test
    fun `여러 미팅 ID로 등록 시 각 스케줄이 독립적으로 존재한다`() {
        // when
        tukNotificationScheduler.scheduleNotification(10L, 1L)
        tukNotificationScheduler.scheduleNotification(20L, 2L)

        // then
        val jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals("notification-job-group"))
        assertThat(jobKeys).hasSize(2)
        assertThat(jobKeys).contains(
            JobKey("10", "notification-job-group"),
            JobKey("20", "notification-job-group")
        )
    }


    @Test
    fun `스케줄된 Job이 지정된 시간에 실행되어 알림을 전송한다`() {
        // given
        val meetingId = 3L
        val durationDays = 0L // 즉시 실행되도록 0일 후로 설정
        val tokens = listOf("token1", "token2")

        every { memberService.findTokensByMeetingId(meetingId) } returns tokens
        every { fcmClient.sendMulticast(any(), any(), any()) } returns Unit

        // when
        tukNotificationScheduler.scheduleNotification(meetingId, durationDays)

        // then
        verify(timeout = 5000) {
            spyTukNotificationJob.execute(any())
        }
    }
}
