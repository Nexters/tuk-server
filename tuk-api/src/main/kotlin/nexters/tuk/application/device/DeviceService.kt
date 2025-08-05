package nexters.tuk.application.device

import nexters.tuk.application.device.dto.request.DeviceCommand
import nexters.tuk.application.device.dto.response.DeviceResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.device.Device
import nexters.tuk.domain.device.DeviceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DeviceService(
    private val deviceRepository: DeviceRepository,
) {
    @Transactional
    fun updateDeviceToken(
        memberId: Long,
        command: DeviceCommand.UpdateDeviceToken,
    ): DeviceResponse.UpdateDeviceToken {
        val deviceToken = command.deviceInfo.deviceToken ?: throw IllegalArgumentException("디바이스 토큰은 필수 정보입니다.")

        val device = deviceRepository.findByDeviceIdAndMemberId(
            deviceId = command.deviceInfo.deviceId,
            memberId = memberId
        )?.let { device ->
            device.updateDeviceToken(
                newDeviceToken = deviceToken,
                newAppVersion = command.deviceInfo.appVersion,
                newOsVersion = command.deviceInfo.osVersion,
            )
            device
        } ?: deviceRepository.save(
            Device.new(
                deviceId = command.deviceInfo.deviceType,
                deviceToken = deviceToken,
                appVersion = command.deviceInfo.appVersion,
                osVersion = command.deviceInfo.osVersion,
                memberId = memberId
            )
        )

        return DeviceResponse.UpdateDeviceToken(
            memberId = device.memberId,
            deviceId = device.deviceId,
            deviceToken = deviceToken,
            updatedAt = device.updatedAt
        )
    }
}