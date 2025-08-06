package nexters.tuk.ui.gathering

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.proposal.ProposalDirection
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse
import nexters.tuk.contract.SliceDto.SliceRequest
import nexters.tuk.contract.SliceDto.SliceResponse
import org.springframework.web.bind.annotation.GetMapping


interface GatheringProposalSpec {
    @Operation(
        summary = "모임 초대장 발행",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun generateProposal(
        memberId: Long,
        gatheringId: Long,
        request: GatheringProposalDto.Request.Publish
    ): ApiResponse<ProposalResponse.Propose>

    @Operation(
        summary = "모임 초대장 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    @GetMapping
    fun getGatheringProposals(
        memberId: Long,
        gatheringId: Long,
        type: ProposalDirection,
        page: SliceRequest
    ): ApiResponse<SliceResponse<ProposalResponse.ProposalOverview>>
}