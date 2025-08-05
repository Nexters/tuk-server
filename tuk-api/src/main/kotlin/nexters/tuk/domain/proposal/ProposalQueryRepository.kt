package nexters.tuk.domain.proposal

import nexters.tuk.application.proposal.ProposalDirection


interface ProposalQueryRepository {
    fun findMemberProposals(
        memberId: Long,
        pageSize: Long,
        pageNumber: Long,
    ): List<ProposalQueryModel.ProposalDetail>

    fun countUnreadMemberProposal(memberId: Long): Long

    fun findGatheringProposals(
        memberId: Long,
        gatheringId: Long,
        type: ProposalDirection,
        pageSize: Long,
        pageNumber: Long
    ): List<ProposalQueryModel.ProposalDetail>
}