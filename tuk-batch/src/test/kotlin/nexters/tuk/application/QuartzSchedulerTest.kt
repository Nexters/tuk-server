package nexters.tuk.application

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.scheduler.GatheringNotificationScheduler
import nexters.tuk.config.FcmConfig
import nexters.tuk.infrastructure.FcmClient
import nexters.tuk.job.TukNotificationJob
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
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
import java.time.temporal.ChronoUnit

@SpringBootTest
@ActiveProfiles("test")
class QuartzSchedulerTest @Autowired constructor(
    private val scheduler: Scheduler,
    private val gatheringNotificationScheduler: GatheringNotificationScheduler,
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
    fun `새로운 모임 ID로 알림을 등록하면 Job과 Trigger가 생성된다`() {
        // given
        val gatheringId = 1L
        val intervalDays = 1L
        val command = GatheringCommand.Notification.Tuk(gatheringId, intervalDays)
        val expectedJobKey = JobKey(gatheringId.toString(), "notification-job-group")
        val expectedTriggerKey = TriggerKey(gatheringId.toString(), "notification-trigger-group")

        // when
        gatheringNotificationScheduler.scheduleTukNotification(command)

        // then
        val jobDetail = scheduler.getJobDetail(expectedJobKey)
        assertThat(jobDetail).isNotNull
        assertThat(jobDetail.key).isEqualTo(expectedJobKey)

        val triggers = scheduler.getTriggersOfJob(expectedJobKey)
        assertThat(triggers).hasSize(1)
        assertThat(triggers.first().key).isEqualTo(expectedTriggerKey)
    }

    @Test
    fun `동일한 모임 ID로 재등록 시 기존 Trigger가 새 정보로 교체된다`() {
        // given
        val gatheringId = 2L
        val initialIntervalDays = 1L
        val updatedIntervalDays = 2L
        val initialCommand = GatheringCommand.Notification.Tuk(gatheringId, initialIntervalDays)
        val updatedCommand = GatheringCommand.Notification.Tuk(gatheringId, updatedIntervalDays)

        // when
        gatheringNotificationScheduler.scheduleTukNotification(initialCommand)
        gatheringNotificationScheduler.scheduleTukNotification(updatedCommand)

        // then
        val jobKey = JobKey(gatheringId.toString(), "notification-job-group")
        val jobDetail = scheduler.getJobDetail(jobKey)
        assertThat(jobDetail).isNotNull
        assertThat(jobDetail.jobDataMap["gatheringId"]).isEqualTo(gatheringId)
        assertThat(jobDetail.jobDataMap["intervalDays"]).isEqualTo(updatedIntervalDays)

        val triggers = scheduler.getTriggersOfJob(jobKey)
        assertThat(triggers).hasSize(1)

        val expectedTime = LocalDateTime.now().plusDays(updatedIntervalDays)
        val triggerTime = triggers.first().startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

        assertThat(triggerTime).isCloseTo(
            expectedTime,
            within(1, ChronoUnit.SECONDS)
        )
    }

    @Test
    fun `여러 모임 ID로 등록 시 각 스케줄이 독립적으로 존재한다`() {
        // given
        val command1 = GatheringCommand.Notification.Tuk(10L, 1L)
        val command2 = GatheringCommand.Notification.Tuk(20L, 2L)

        // when
        gatheringNotificationScheduler.scheduleTukNotification(command1)
        gatheringNotificationScheduler.scheduleTukNotification(command2)

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
        val gatheringId = 3L
        val intervalDays = 0L // 즉시 실행되도록 0일 후로 설정
        val command = GatheringCommand.Notification.Tuk(gatheringId, intervalDays)
        val tokens = listOf("token1", "token2")

        every { memberService.findTokensByGatheringId(gatheringId) } returns tokens
        every { fcmClient.notifyMembers(any(), any(), any()) } returns Unit

        // when
        gatheringNotificationScheduler.scheduleTukNotification(command)

        // then
        verify(timeout = 5000) {
            spyTukNotificationJob.execute(any())
        }
    }
}
