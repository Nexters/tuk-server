package nexters.tuk.domain.proposal

import nexters.tuk.application.proposal.ProposalDirection
import nexters.tuk.contract.SliceDto.SliceRequest


interface ProposalQueryRepository {
    fun findMemberProposals(
        memberId: Long,
        page: SliceRequest
    ): List<ProposalQueryModel.ProposalOverview>

    fun countUnreadMemberProposal(memberId: Long): Long

    fun findGatheringProposals(
        memberId: Long,
        gatheringId: Long,
        type: ProposalDirection,
        page: SliceRequest
    ): List<ProposalQueryModel.ProposalOverview>

    fun findProposalById(proposalId: Long): ProposalQueryModel.ProposalDetail?
}