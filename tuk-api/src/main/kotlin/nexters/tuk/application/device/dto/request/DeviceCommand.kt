package nexters.tuk.application.device.dto.request

import nexters.tuk.contract.device.TukDeviceInfo

class DeviceCommand {
    data class UpdateDeviceToken(
        val deviceInfo: TukDeviceInfo,
    )
}