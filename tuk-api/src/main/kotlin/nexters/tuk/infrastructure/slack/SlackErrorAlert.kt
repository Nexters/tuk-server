package nexters.tuk.infrastructure.slack

import com.slack.api.model.block.Blocks
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.composition.MarkdownTextObject
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class SlackErrorAlert(
    val statusCode: Int,
    val httpMethod: String,
    val path: String,
    val occurredAt: ZonedDateTime,
    val errorMessage: String,
    val requestParamsJson: String
) {
    private fun kstString(): String =
        occurredAt.withZoneSameInstant(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))

    fun toBlocks(): List<LayoutBlock> {
        val isServerError = statusCode >= 500
        val blocks = mutableListOf<LayoutBlock>()

        // 5xx면 맨 위에 멘션(알림 트리거)
        if (isServerError) {
            blocks += Blocks.section { it.text(MarkdownTextObject("<!here>", false)) }
        }

        // 제목은 header 블록(가독성 최고)
        blocks += Blocks.header {
            it.text(BlockCompositions.plainText("🚨 [$statusCode] Internal Server Error", true))
        }

        // 요약(마크다운 불릿 리스트로 시원하게)
        val summaryMd = """
            *요약*
            • *상태 코드*: `$statusCode`
            • *HTTP 메서드*: `$httpMethod`
            • *경로*: `$path`
            • *발생 시각*: `${kstString()}`
        """.trimIndent()
        blocks += Blocks.section { it.text(MarkdownTextObject(summaryMd, false)) }

        blocks += Blocks.divider()

        // 에러 메시지(코드블록)
        val errorMd = "❌ *에러 메시지*\n```$errorMessage```"
        blocks += Blocks.section { it.text(MarkdownTextObject(errorMd, false)) }

        // 요청 파라미터(코드블록, 비어있지 않을 때만)
        if (requestParamsJson.isNotBlank()) {
            val paramsMd = "📦 *요청 파라미터*\n```$requestParamsJson```"
            blocks += Blocks.section { it.text(MarkdownTextObject(paramsMd, false)) }
        }

        return blocks
    }

    // 검색/모바일용 폴백 텍스트(5xx는 멘션 포함)
    fun fallbackText(): String {
        val prefix = if (statusCode >= 500) "<!here> " else ""
        return prefix + "[$statusCode] $httpMethod $path @ ${kstString()}: $errorMessage"
    }
}