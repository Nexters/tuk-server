package nexters.tuk.ui.gathering

import nexters.tuk.application.proposal.ProposalDirection
import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.dto.request.ProposalQuery
import nexters.tuk.application.proposal.vo.ProposalPurpose

class GatheringProposalDto {
    class Request {
        data class Publish(
            val purpose: ProposalPurpose,
        ) {
            fun toCommand(memberId: Long, gatheringId: Long): ProposalCommand.Propose {
                return ProposalCommand.Propose(
                    memberId = memberId,
                    gatheringId = gatheringId,
                    purpose = purpose
                )
            }
        }

        data class GatheringProposals(
            val type: ProposalDirection,
            val pageSize: Long,
            val pageNumber: Long,
        ) {
            fun toQuery(memberId: Long, gatheringId: Long): ProposalQuery.GatheringProposals {
                return ProposalQuery.GatheringProposals(
                    memberId = memberId,
                    gatheringId = gatheringId,
                    type = type,
                    pageSize = pageSize,
                    pageNumber = pageNumber
                )
            }
        }
    }
}