package nexters.tuk.ui.proposal

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse

interface ProposalSpec {
    @Operation(
        summary = "사용자 만남 초대장 전체 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun getMemberProposals(
        @Parameter(hidden = true) memberId: Long,
        request: ProposalDto.Request.MemberProposals
    ): ApiResponse<ProposalResponse.MemberProposals>
}