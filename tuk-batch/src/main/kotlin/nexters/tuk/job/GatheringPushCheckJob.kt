package nexters.tuk.job

import nexters.tuk.application.GatheringService
import nexters.tuk.contract.push.PushType
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.push.PushApiClient
import nexters.tuk.domain.push.PushDto
import org.quartz.CronScheduleBuilder
import org.quartz.CronTrigger
import org.quartz.JobExecutionContext
import org.quartz.ScheduleBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class GatheringPushCheckJob(
    private val gatheringRepository: GatheringRepository,
    private val gatheringService: GatheringService,
    private val pushApiClient: PushApiClient,
) : AbstractJob<CronTrigger>(), TukJob {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getScheduleBuilder(): ScheduleBuilder<CronTrigger> {
        return CronScheduleBuilder.cronSchedule("0 */10 * * * ?")
            .withMisfireHandlingInstructionDoNothing();
    }

    override fun executeInternal(context: JobExecutionContext) {
        logger.info("[GatheringPushCheckJob] 모임 푸시 알림 체크 시작")
        val gatherings = gatheringRepository.getAllGatheringsWithMember()

        gatherings.forEach { gathering ->
            val nextNotificationTime = if (gathering.lastPushedAt != null) {
                gathering.lastPushedAt.plusDays(gathering.intervalDays)
            } else {
                // 첫 번째 푸시인 경우 생성일 기준으로 알림 시간 설정
                gathering.createdAt.plusDays(gathering.intervalDays)
            }

            if (nextNotificationTime <= LocalDateTime.now()) {
                pushApiClient.sendPushNotification(
                    PushDto.Push(
                        gatheringId = gathering.id,
                        pushType = PushType.GATHERING_NOTIFICATION
                    )
                )
                gatheringService.updatePushStatus(gatheringId = gathering.id)

                logger.info("[알림 전송 완료] 모임 ID: ${gathering.id})")
            }
        }
        logger.info("[GatheringPushCheckJob] 모임 푸시 알림 체크 완료")
    }
}