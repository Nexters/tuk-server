package nexters.tuk.ui.proposal

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse
import nexters.tuk.contract.SliceDto.SliceRequest
import nexters.tuk.contract.SliceDto.SliceResponse

interface MemberProposalSpec {
    @Operation(
        summary = "사용자 만남 초대장 전체 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun getMemberProposals(
        memberId: Long,
        request: SliceRequest
    ): ApiResponse<SliceResponse<ProposalResponse.ProposalOverview>>
}