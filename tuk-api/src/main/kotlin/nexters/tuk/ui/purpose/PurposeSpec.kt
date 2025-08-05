package nexters.tuk.ui.purpose

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.purpose.dto.response.PurposeResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse

interface PurposeSpec {
    @Operation(
        summary = "여행 목적 전체 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun getAllPurposes(): ApiResponse<PurposeResponse.Purposes>
}