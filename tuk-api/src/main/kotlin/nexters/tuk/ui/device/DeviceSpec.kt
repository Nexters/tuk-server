package nexters.tuk.ui.device

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.device.dto.request.DeviceCommand
import nexters.tuk.application.device.dto.response.DeviceResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.RequestBody

interface DeviceSpec {
    @Operation(
        summary = "디바이스 토큰 업데이트",
        description = "사용자의 디바이스 토큰을 업데이트합니다. 신규 디바이스인 경우 등록하고, 기존 디바이스인 경우 토큰을 업데이트합니다.",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun updateDeviceToken(
        memberId: Long,
        @RequestBody request: DeviceCommand.UpdateDeviceToken,
    ): ApiResponse<DeviceResponse.UpdateDeviceToken>
}