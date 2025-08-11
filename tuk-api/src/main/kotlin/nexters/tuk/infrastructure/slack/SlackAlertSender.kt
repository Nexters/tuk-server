package nexters.tuk.infrastructure.slack

import com.slack.api.methods.MethodsClient
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class SlackAlertSender(
    private val errorAlertSlackClient: MethodsClient,
    @Value("\${slack.channels.error-alert}")
    private val alertChannel: String,
) {

    @Async
    fun sendAlert(slackErrorAlert: SlackErrorAlert) {
        alertChannel.takeIf { it.isNotBlank() }
            ?.let { channel ->
                runCatching {
                    errorAlertSlackClient.chatPostMessage { req ->
                        req.channel(channel)
                            .blocks(slackErrorAlert.toBlocks())
                            .text(slackErrorAlert.fallbackText()) // 폴백 텍스트 추가
                    }
                }.onSuccess { response ->
                    if (!response.isOk) {
                        logger.error { "Failed to send Slack alert: ${response.error}" }
                    }
                }.onFailure { exception ->
                    logger.error(exception) { "Error sending Slack alert" }
                }
            } ?: logger.warn { "Slack alert channel is not configured. Skipping alert." }
    }
}