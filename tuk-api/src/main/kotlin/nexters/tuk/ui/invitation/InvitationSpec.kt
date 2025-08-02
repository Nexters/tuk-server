package nexters.tuk.ui.invitation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.invitation.dto.response.InvitationResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse


interface InvitationSpec {
    @Operation(
        summary = "초대장 발행",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun generateInvitation(
        @Parameter(hidden = true) memberId: Long,
        request: InvitationDto.Request.Publish
    ): ApiResponse<InvitationResponse.Publish>
}