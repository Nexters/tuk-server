package nexters.tuk.infrastructure.slack

import com.slack.api.methods.MethodsClient
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.composition.MarkdownTextObject
import mu.KotlinLogging
import nexters.tuk.application.alert.ApiErrorAlert
import nexters.tuk.application.alert.ApiErrorAlertSender
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Component
class SlackApiErrorAlertSender(
    private val errorAlertSlackClient: MethodsClient,
    @Value("\${slack.channels.error-alert}")
    private val alertChannel: String,
) : ApiErrorAlertSender {

    @Async
    override fun sendError(apiErrorAlert: ApiErrorAlert) {
        alertChannel.takeIf { it.isNotBlank() }
            ?.let { channel ->
                runCatching {
                    errorAlertSlackClient.chatPostMessage { req ->
                        req.channel(channel)
                            .blocks(createBlocks(apiErrorAlert))
                            .text(createFallbackText(apiErrorAlert))
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

    private fun createBlocks(alert: ApiErrorAlert): List<LayoutBlock> {
        val isServerError = alert.statusCode >= HttpStatus.INTERNAL_SERVER_ERROR.value()
        val blocks = mutableListOf<LayoutBlock>()

        if (isServerError) {
            blocks += Blocks.section { it.text(MarkdownTextObject("<!here>", false)) }
        }

        blocks += Blocks.header {
            it.text(
                BlockCompositions.plainText(
                    "🚨 [${alert.statusCode}] Internal Server Error",
                    true
                )
            )
        }

        val summaryMd = """
            *요약*
            • *상태 코드*: `${alert.statusCode}`
            • *HTTP 메서드*: `${alert.httpMethod}`
            • *경로*: `${alert.path}`
            • *발생 시각*: `${formatKstTime(alert)}`
        """.trimIndent()
        blocks += Blocks.section { it.text(MarkdownTextObject(summaryMd, false)) }

        blocks += Blocks.divider()

        val errorMd = "❌ *에러 메시지*\n```${alert.errorMessage}```"
        blocks += Blocks.section { it.text(MarkdownTextObject(errorMd, false)) }

        return blocks
    }

    private fun createFallbackText(alert: ApiErrorAlert): String {
        val prefix = if (alert.statusCode >= HttpStatus.INTERNAL_SERVER_ERROR.value()) "<!here> " else ""
        return prefix + "[${alert.statusCode}] ${alert.httpMethod} ${alert.path} @ ${
            formatKstTime(
                alert
            )
        }: ${alert.errorMessage}"
    }

    private fun formatKstTime(alert: ApiErrorAlert): String =
        alert.occurredAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
}