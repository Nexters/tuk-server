package nexters.tuk.application.push

import nexters.tuk.application.push.dto.request.PushCommand
import nexters.tuk.domain.device.DeviceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushService(
    private val pushSender: PushSender,
    private val deviceRepository: DeviceRepository,
) {
    private val logger = LoggerFactory.getLogger(PushService::class.java)

    @Transactional
    fun sendPush(command: PushCommand.Push) {
        logger.info("Sending bulk push notification. Recipients: ${command.recipients.size}")
        val memberIds = command.recipients.map { it.memberId }
        val deviceTokens = deviceRepository.findByMemberIdIn(memberIds)
            .mapNotNull { device -> device.deviceToken?.let { DeviceToken(it) } }

        pushSender.send(
            deviceTokens = deviceTokens,
            message = command.message
        )
        logger.info("Sent push notification. Recipients: ${command.recipients.size}")
    }
}