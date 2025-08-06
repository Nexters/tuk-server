package nexters.tuk.ui.gathering

import nexters.tuk.application.proposal.ProposalCreateService
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
    private val proposalCreateService: ProposalCreateService,
    private val proposalQueryService: ProposalQueryService,
) : GatheringProposalSpec {

    @PostMapping
    override fun generateProposal(
        @Authenticated memberId: Long,
        @PathVariable("gatheringId") gatheringId: Long,
        @RequestBody request: GatheringProposalDto.Request.Publish
    ): ApiResponse<ProposalResponse.Propose> {
        val response = proposalCreateService.propose(request.toCommand(memberId, gatheringId))

        return ApiResponse.ok(response)
    }

    @GetMapping
    override fun getGatheringProposals(
        @Authenticated memberId: Long,
        @PathVariable gatheringId: Long,
        @RequestParam type: ProposalDirection,
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
}