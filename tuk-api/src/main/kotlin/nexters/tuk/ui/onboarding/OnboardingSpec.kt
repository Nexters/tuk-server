package nexters.tuk.ui.onboarding

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.onboarding.dto.response.OnboardingResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse

interface OnboardingSpec {

    @Operation(
        summary = "온보딩에 필요한 필드 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun getRequiredFields(memberId: Long): ApiResponse<OnboardingResponse.RequiredFields>

    @Operation(
        summary = "온보딩 진행",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun initInfo(
        memberId: Long,
        request: OnboardingDto.Request.Init
    ): ApiResponse<OnboardingResponse.Init>
}