package nexters.tuk.application.push.dto.request

class PushCommand {
    data class Push(
        val recipients: List<PushRecipient>,
        val message: MessagePayload,
    )

    data class MessagePayload(
        val title: String,
        val body: String,
    )

    // FIXME: 디바이스 토큰이 아닌 memberId만 받아서 직접 토큰 조회후 발송하도록 책임 분리해도 좋을듯
    data class PushRecipient(
        val deviceToken: String,
        val memberId: Long? = null,
    )
}