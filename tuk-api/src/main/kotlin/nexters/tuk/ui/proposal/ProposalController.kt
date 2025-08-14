package nexters.tuk.ui.proposal

import nexters.tuk.application.proposal.ProposalCreateService
import nexters.tuk.application.proposal.ProposalQueryService
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/proposals")
class ProposalController(
    private val proposalQueryService: ProposalQueryService,
    private val proposalCreateService: ProposalCreateService
) : ProposalSpec {
    @GetMapping("/{proposalId}")
    override fun getProposal(@PathVariable proposalId: Long): ApiResponse<ProposalResponse.ProposalDetail> {
        val response = proposalQueryService.getProposal(proposalId)

        return ApiResponse.ok(response)
    }

    @PostMapping
    override fun generateProposal(
        @Authenticated memberId: Long,
        @RequestBody request: ProposalDto.Request.Publish
    ): ApiResponse<ProposalResponse.Propose> {
        val response = proposalCreateService.propose(request.toCommand(memberId))

        return ApiResponse.ok(response)
    }
}