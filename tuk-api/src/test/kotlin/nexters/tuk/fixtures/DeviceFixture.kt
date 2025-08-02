package nexters.tuk.fixtures

import nexters.tuk.contract.device.TukClientInfo

fun createDeviceInfoFixture(
    deviceId: String = "1",
    deviceToken: String = "token",
    deviceType: String = "ios",
    appVersion: String = "1.0",
    osVersion: String = "1.0",
): TukClientInfo {
    return TukClientInfo(
        deviceId = deviceId,
        deviceToken = deviceToken,
        deviceType = deviceType,
        appVersion = appVersion,
        osVersion = osVersion,
    )
}