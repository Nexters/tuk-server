package nexters.tuk.application.push

import nexters.tuk.application.push.dto.request.PushCommand
import nexters.tuk.application.push.dto.response.PushResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushService(
    private val pushSender: PushSender,
) {
    private val logger = LoggerFactory.getLogger(PushService::class.java)

    @Transactional
    fun sendPush(command: PushCommand.Push) {
        logger.info("Sending bulk push notification. Recipients: ${command.recipients.size}")

        pushSender.send(
            recipients = command.recipients,
            message = command.message
        )
        // TODO: Gathering lastPushedAt 업데이트
    }
}