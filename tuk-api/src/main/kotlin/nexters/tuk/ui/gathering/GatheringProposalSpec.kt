package nexters.tuk.ui.gathering

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.proposal.ProposalDirection
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse
import nexters.tuk.contract.SliceDto.SliceRequest
import nexters.tuk.contract.SliceDto.SliceResponse


interface GatheringProposalSpec {
    @Operation(
        summary = "만남 초대장 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun getGatheringProposals(
        memberId: Long,
        gatheringId: Long,
        type: ProposalDirection,
        page: SliceRequest
    ): ApiResponse<SliceResponse<ProposalResponse.ProposalOverview>>
}