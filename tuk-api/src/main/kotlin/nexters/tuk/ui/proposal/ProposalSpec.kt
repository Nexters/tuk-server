package nexters.tuk.ui.proposal

import io.swagger.v3.oas.annotations.Operation
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.contract.ApiResponse

interface ProposalSpec {
    @Operation(
        summary = "[비회원] 만남 초대장 상세 조회",
    )
    fun getProposal(proposalId: Long): ApiResponse<ProposalResponse.ProposalDetail>
}