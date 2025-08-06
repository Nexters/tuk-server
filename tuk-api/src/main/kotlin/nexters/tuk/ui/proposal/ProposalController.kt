package nexters.tuk.ui.proposal

import nexters.tuk.application.proposal.ProposalQueryService
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/proposals")
class ProposalController(
    private val proposalQueryService: ProposalQueryService,
) : ProposalSpec {
    @GetMapping("/{proposalId}")
    override fun getProposal(@PathVariable proposalId: Long): ApiResponse<ProposalResponse.ProposalDetail> {
        val response = proposalQueryService.getProposal(proposalId)

        return ApiResponse.ok(response)
    }
}