package nexters.tuk.ui.member

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.member.dto.response.MemberResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse

interface MemberSpec {

    @Operation(
        summary = "온보딩 진행",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun executeOnboarding(
        memberId: Long,
        request: MemberDto.Request.Onboarding
    ): ApiResponse<MemberResponse.Onboarding>
}