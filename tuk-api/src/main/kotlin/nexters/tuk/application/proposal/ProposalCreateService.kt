package nexters.tuk.application.proposal

import nexters.tuk.application.gathering.GatheringProposalService
import nexters.tuk.application.gathering.dto.request.GatheringProposalCommand
import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProposalCreateService(
    private val proposalService: ProposalService,
    private val gatheringProposalService: GatheringProposalService,
) {

    @Transactional
    fun propose(command: ProposalCommand.Propose): ProposalResponse.Propose {
        val proposal = proposalService.propose(command)

        if (command.gatheringId != null) {
            gatheringProposalService.addProposal(
                GatheringProposalCommand.AddProposal(
                    memberId = command.memberId,
                    gatheringId = command.gatheringId,
                    proposalId = proposal.proposalId
                )
            )
        }

        return ProposalResponse.Propose(proposalId = proposal.proposalId)
    }
}