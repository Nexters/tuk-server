package nexters.tuk.application.push

import nexters.tuk.application.device.DeviceService
import nexters.tuk.application.gathering.GatheringMemberService
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.push.dto.request.PushCommand
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushService(
    private val pushSender: PushSender,
    private val deviceService: DeviceService,
    private val gatheringMemberService: GatheringMemberService,
    private val memberService: MemberService,
) {
    private val logger = LoggerFactory.getLogger(PushService::class.java)

    @Transactional
    fun sendPush(command: PushCommand.Push) {
        val pushMessage = PushMessage.random()
        when (command) {
            is PushCommand.Push.GatheringNotification -> {
                val memberIds = command.recipients.map { it.memberId }
                pushAll(memberIds = memberIds, pushMessage = pushMessage)
                logger.info("Sent gathering notification push. Recipients: ${command.recipients.size}, PushType: ${command.pushType}")
            }

            is PushCommand.Push.Proposal -> {
                val memberIds = gatheringMemberService.getGatheringMemberIds(gatheringId = command.gatheringId)
                pushAll(memberIds = memberIds, pushMessage = pushMessage)
                logger.info("Sent proposal push. GatheringId: ${command.gatheringId}, Recipients: ${memberIds.size}, PushType: ${command.pushType}")
            }
        }
    }

    private fun pushAll(
        memberIds: List<Long>,
        pushMessage: PushMessage,
    ) {
        val memberNameMap = memberService.getMembers(memberIds).associate { it.memberId to it.memberName }
        deviceService.getDeviceTokens(memberIds).forEach { token ->
            pushSender.send(
                deviceTokens = listOf(DeviceToken(token.deviceToken)),
                message = PushCommand.MessagePayload(
                    title = pushMessage.getTitle(
                        memberNameMap[token.memberId]
                            ?: return@forEach
                    ),
                    body = pushMessage.body
                )
            )
        }
    }
}