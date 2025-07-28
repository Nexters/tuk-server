package nexters.tuk.application.push

import nexters.tuk.application.push.dto.request.PushCommand
import nexters.tuk.application.push.dto.response.PushResponse

interface PushSender {
    fun send(
        recipients: List<PushCommand.PushRecipient>,
        message: PushCommand.MessagePayload,
    ): PushResponse.Push
}