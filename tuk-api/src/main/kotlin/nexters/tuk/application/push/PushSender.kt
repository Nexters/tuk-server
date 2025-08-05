package nexters.tuk.application.push

import nexters.tuk.application.push.dto.request.PushCommand

@JvmInline
value class DeviceToken(val token: String) {
    init {
        require(token.isNotBlank()) { "Device token must not be blank." }
    }
}

interface PushSender {
    fun send(
        deviceTokens: List<DeviceToken>,
        message: PushCommand.MessagePayload,
    )
}