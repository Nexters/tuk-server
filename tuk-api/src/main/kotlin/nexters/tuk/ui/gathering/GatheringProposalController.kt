package nexters.tuk.ui.gathering

import nexters.tuk.application.proposal.ProposalCreateService
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/gatherings/{gatheringId}/proposals")
class GatheringProposalController(
    private val proposalCreateService: ProposalCreateService,
) : GatheringProposalSpec {

    @PostMapping
    override fun generateProposal(
        @Authenticated memberId: Long,
        @PathVariable("gatheringId") gatheringId: Long,
        request: GatheringProposalDto.Request.Publish
    ): ApiResponse<ProposalResponse.Propose> {
        val response = proposalCreateService.propose(request.toCommand(memberId, gatheringId))

        return ApiResponse.ok(response)
    }
}