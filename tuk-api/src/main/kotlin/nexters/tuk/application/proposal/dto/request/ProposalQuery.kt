package nexters.tuk.application.proposal.dto.request

import nexters.tuk.application.proposal.ProposalDirection

class ProposalQuery {
    data class MemberProposals(
        val memberId: Long,
        val pageSize: Long,
        val pageNumber: Long,
    )

    data class GatheringProposals(
        val memberId: Long,
        val gatheringId: Long,
        val type: ProposalDirection,
        val pageSize: Long,
        val pageNumber: Long,
    )
}