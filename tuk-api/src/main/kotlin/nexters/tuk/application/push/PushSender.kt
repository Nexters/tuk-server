package nexters.tuk.application.push

import nexters.tuk.application.push.dto.request.PushCommand

@JvmInline
value class DeviceToken(val token: String)

interface PushSender {
    fun send(
        deviceTokens: List<DeviceToken>,
        message: PushCommand.MessagePayload,
    )
}