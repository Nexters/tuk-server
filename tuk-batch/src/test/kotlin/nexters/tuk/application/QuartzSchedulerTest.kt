package nexters.tuk.application

import nexters.tuk.application.scheduler.dto.request.GatheringCommand
import nexters.tuk.scheduler.GatheringNotificationScheduler
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerKey
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@SpringBootTest
class QuartzSchedulerTest @Autowired constructor(
    private val scheduler: Scheduler,
    private val gatheringNotificationScheduler: GatheringNotificationScheduler,
) {

    @AfterEach
    fun tearDown() {
        scheduler.clear()
    }

    @Test
    fun `새로운 모임 ID로 알림을 등록하면 Job과 Trigger가 생성된다`() {
        // given
        val gatheringId = "1"
        val sendAt = LocalDateTime.now().plusDays(1)
        val command = GatheringCommand.Notification(gatheringId, sendAt)
        val expectedJobKey = JobKey(gatheringId, "notification-job-group")
        val expectedTriggerKey = TriggerKey(gatheringId, "notification-trigger-group")

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
        val gatheringId = "2"
        val initialSendAt = LocalDateTime.now().plusDays(1)
        val updatedSendAt = LocalDateTime.now().plusDays(2)
        val initialCommand = GatheringCommand.Notification(gatheringId, initialSendAt)
        val updatedCommand = GatheringCommand.Notification(gatheringId, updatedSendAt)

        // when
        gatheringNotificationScheduler.scheduleTukNotification(initialCommand)
        gatheringNotificationScheduler.scheduleTukNotification(updatedCommand)

        // then
        val jobKey = JobKey(gatheringId, "notification-job-group")
        val jobDetail = scheduler.getJobDetail(jobKey)
        assertThat(jobDetail).isNotNull

        val triggers = scheduler.getTriggersOfJob(jobKey)
        assertThat(triggers).hasSize(1)

        val triggerTime = triggers.first().startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

        assertThat(triggerTime).isCloseTo(
            updatedSendAt,
            within(1, ChronoUnit.SECONDS)
        )
    }

    @Test
    fun `여러 모임 ID로 등록 시 각 스케줄이 독립적으로 존재한다`() {
        // given
        val command1 = GatheringCommand.Notification("10", LocalDateTime.now().plusDays(1))
        val command2 = GatheringCommand.Notification("20", LocalDateTime.now().plusDays(2))

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
}
