package nexters.tuk.application.proposal.dto.request

class ProposalQuery {
    data class MemberProposals(
        val memberId: Long,
        val pageSize: Long,
        val pageNumber: Long,
    )
}