package nexters.tuk.application.push.dto.request

import nexters.tuk.application.push.PushType

class PushCommand {
    data class Push(
        val recipients: List<PushRecipient>,
        val message: MessagePayload,
        val pushType: PushType,
    )

    data class MessagePayload(
        val title: String,
        val body: String,
    )

    data class PushRecipient(
        val memberId: Long,
    )
}