package nexters.tuk.ui.gathering

import nexters.tuk.application.gathering.GatheringProposalService
import nexters.tuk.application.proposal.ProposalDirection
import nexters.tuk.application.proposal.ProposalQueryService
import nexters.tuk.application.proposal.dto.request.ProposalQuery
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.contract.SliceDto.SliceRequest
import nexters.tuk.contract.SliceDto.SliceResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/gatherings/{gatheringId}/proposals")
class GatheringProposalController(
    private val proposalQueryService: ProposalQueryService,
    private val gatheringProposalService: GatheringProposalService,
) : GatheringProposalSpec {

    @GetMapping("/{type}")
    override fun getGatheringProposals(
        @Authenticated memberId: Long,
        @PathVariable("gatheringId") gatheringId: Long,
        @PathVariable("type") type: ProposalDirection,
        @ModelAttribute page: SliceRequest
    ): ApiResponse<SliceResponse<ProposalResponse.ProposalOverview>> {
        val response = proposalQueryService.getGatheringProposals(
            ProposalQuery.GatheringProposals(
                memberId = memberId,
                gatheringId = gatheringId,
                type = type,
                page = page
            )
        )

        return ApiResponse.ok(response)
    }

    @PostMapping
    override fun addProposal(
        @Authenticated memberId: Long,
        @PathVariable("gatheringId") gatheringId: Long,
        request: GatheringProposalDto.Request.AddProposal
    ): ApiResponse<Unit> {
        gatheringProposalService.addProposal(
            request.toCommand(
                memberId = memberId,
                gatheringId = gatheringId
            )
        )

        return ApiResponse.ok(Unit)
    }
}