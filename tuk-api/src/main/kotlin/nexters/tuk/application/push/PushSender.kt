package nexters.tuk.application.push

import nexters.tuk.application.push.dto.request.PushCommand

interface PushSender {
    fun send(
        recipients: List<PushCommand.PushRecipient>,
        message: PushCommand.MessagePayload,
    )
}