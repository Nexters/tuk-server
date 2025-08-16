package nexters.tuk.ui.proposal

import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.vo.ProposalPurposeInfo

class ProposalDto {
    class Request {
        data class Publish(
            val gatheringId: Long?,
            val purpose: ProposalPurposeInfo,
        ) {
            fun toCommand(memberId: Long): ProposalCommand.Propose {
                return ProposalCommand.Propose(
                    memberId = memberId,
                    purpose = purpose,
                    gatheringId = gatheringId
                )
            }
        }
    }
}