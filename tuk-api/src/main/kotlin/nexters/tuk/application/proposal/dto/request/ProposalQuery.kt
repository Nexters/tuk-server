package nexters.tuk.application.proposal.dto.request

import nexters.tuk.application.proposal.ProposalDirection
import nexters.tuk.contract.SliceDto.SliceRequest

class ProposalQuery {
    data class MemberProposals(
        val memberId: Long,
        val page: SliceRequest,
    )

    data class GatheringProposals(
        val memberId: Long,
        val gatheringId: Long,
        val type: ProposalDirection,
        val page: SliceRequest,
    )
}