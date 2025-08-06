package nexters.tuk.application.proposal.dto.request

import nexters.tuk.application.proposal.vo.ProposalPurposeInfo

class ProposalCommand {
    data class Propose(
        val memberId: Long,
        val gatheringId: Long,
        val purpose: ProposalPurposeInfo
    )
}