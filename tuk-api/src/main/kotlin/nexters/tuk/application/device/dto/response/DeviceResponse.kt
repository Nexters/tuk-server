package nexters.tuk.application.device.dto.response

import java.time.LocalDateTime

class DeviceResponse {
    data class UpdateDeviceToken(
        val memberId: Long,
        val deviceId: String,
        val deviceToken: String,
        val updatedAt: LocalDateTime,
    )
}