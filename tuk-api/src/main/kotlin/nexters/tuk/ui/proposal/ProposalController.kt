package nexters.tuk.ui.proposal

import nexters.tuk.application.proposal.ProposalQueryService
import nexters.tuk.application.proposal.dto.request.ProposalQuery
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.contract.SliceDto.SliceRequest
import nexters.tuk.contract.SliceDto.SliceResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/proposals")
class ProposalController(
    private val proposalQueryService: ProposalQueryService
) : ProposalSpec {
    @GetMapping
    override fun getMemberProposals(
        @Authenticated memberId: Long,
        @ModelAttribute request: SliceRequest
    ): ApiResponse<SliceResponse<ProposalResponse.ProposalOverview>> {

        val response = proposalQueryService.getMemberProposals(
            ProposalQuery.MemberProposals(
                memberId = memberId,
                page = request,
            )
        )

        return ApiResponse.ok(response)
    }
}