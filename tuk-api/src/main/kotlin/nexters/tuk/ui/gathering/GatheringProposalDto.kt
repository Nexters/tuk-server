package nexters.tuk.ui.gathering

import nexters.tuk.application.gathering.dto.request.GatheringProposalCommand

class GatheringProposalDto {
    class Request {
        data class AddProposal(
            val proposalId: Long
        ) {
            fun toCommand(memberId: Long, gatheringId: Long): GatheringProposalCommand.AddProposal {
                return GatheringProposalCommand.AddProposal(
                    memberId = memberId,
                    gatheringId = gatheringId,
                    proposalId = proposalId
                )
            }
        }
    }
}