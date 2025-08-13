package nexters.tuk.ui.proposal

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse

interface ProposalSpec {
    @Operation(
        summary = "[비회원] 만남 초대장 상세 조회",
    )
    fun getProposal(proposalId: Long): ApiResponse<ProposalResponse.ProposalDetail>

    @Operation(
        summary = "만남 초대장 발행",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun generateProposal(
        memberId: Long,
        request: ProposalDto.Request.Publish
    ): ApiResponse<ProposalResponse.Propose>
}