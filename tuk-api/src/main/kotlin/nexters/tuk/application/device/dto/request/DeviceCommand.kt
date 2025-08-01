package nexters.tuk.application.device.dto.request

import nexters.tuk.contract.device.TukClientInfo

class DeviceCommand {
    data class UpdateDeviceToken(
        val deviceInfo: TukClientInfo,
    )
}