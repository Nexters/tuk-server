package nexters.tuk.ui.proposal

import nexters.tuk.application.proposal.dto.request.ProposalQuery

class ProposalDto {
    class Request {
        data class MemberProposals(
            val size: Long = 10,
            val page: Long = 0
        ) {
            fun toQuery(memberId: Long): ProposalQuery.MemberProposals {
                return ProposalQuery.MemberProposals(
                    memberId = memberId,
                    pageSize = size,
                    pageNumber = page
                )
            }
        }
    }
}