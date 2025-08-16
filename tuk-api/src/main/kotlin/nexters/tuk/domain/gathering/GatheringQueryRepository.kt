package nexters.tuk.domain.gathering

interface GatheringQueryRepository {
    fun findGatheringMemberProposalState(
        gatheringId: Long,
        memberId: Long
    ): GatheringQueryModel.ProposalState
}
