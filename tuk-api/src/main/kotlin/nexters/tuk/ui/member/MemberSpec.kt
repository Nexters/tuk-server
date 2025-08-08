package nexters.tuk.ui.member

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.member.dto.response.MemberResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse

interface MemberSpec {
    @Operation(
        summary = "회원 탈퇴",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun deleteMember(memberId: Long): ApiResponse<Unit>

    @Operation(
        summary = "회원 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun getMember(memberId: Long): ApiResponse<MemberResponse.Profile>
}