package nexters.tuk.domain.proposal


interface ProposalQueryRepository {
    fun findMemberProposals(
        memberId: Long,
        pageSize: Long,
        pageNumber: Long,
    ): List<ProposalQueryModel.ProposalDetail>

    fun countUnreadMemberProposal(memberId: Long): Long
}