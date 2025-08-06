package nexters.tuk.ui.gathering

import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.vo.ProposalPurposeInfo

class GatheringProposalDto {
    class Request {
        data class Publish(
            val purpose: ProposalPurposeInfo,
        ) {
            fun toCommand(memberId: Long, gatheringId: Long): ProposalCommand.Propose {
                return ProposalCommand.Propose(
                    memberId = memberId,
                    gatheringId = gatheringId,
                    purpose = purpose
                )
            }
        }
    }
}