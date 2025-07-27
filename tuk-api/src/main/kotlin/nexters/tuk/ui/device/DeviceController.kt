package nexters.tuk.ui.device

import nexters.tuk.application.device.DeviceService
import nexters.tuk.application.device.dto.request.DeviceCommand
import nexters.tuk.application.device.dto.response.DeviceResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/device")
class DeviceController(
    private val deviceService: DeviceService,
) : DeviceSpec {

    @PostMapping("/token")
    override fun updateDeviceToken(
        @Authenticated memberId: Long,
        @RequestBody request: DeviceCommand.UpdateDeviceToken,
    ): ApiResponse<DeviceResponse.UpdateDeviceToken> {
        val result = deviceService.updateDeviceToken(
            memberId = memberId,
            command = request
        )
        return ApiResponse.ok(result)
    }
}