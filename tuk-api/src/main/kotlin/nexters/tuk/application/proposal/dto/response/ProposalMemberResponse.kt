package nexters.tuk.application.proposal.dto.response

class ProposalMemberResponse {
    data class PublishedProposalMembers(
        val proposalMemberIds: List<Long>,
    )
}