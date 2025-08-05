package nexters.tuk.ui.gathering

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.GetMapping


interface GatheringProposalSpec {
    @Operation(
        summary = "모임 초대장 발행",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun generateProposal(
        @Parameter(hidden = true) memberId: Long,
        gatheringId: Long,
        request: GatheringProposalDto.Request.Publish
    ): ApiResponse<ProposalResponse.Propose>

    @Operation(
        summary = "모임 초대장 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    @GetMapping
    fun getGatheringProposals(
        @Parameter(hidden = true) memberId: Long,
        gatheringId: Long,
        request: GatheringProposalDto.Request.GatheringProposals
    ): ApiResponse<ProposalResponse.GatheringProposals>
}