package nexters.tuk.application.proposal

import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProposalCreateService(
    private val proposalService: ProposalService,
) {
    @Transactional
    fun propose(command: ProposalCommand.Propose): ProposalResponse.Propose {
        val proposal = proposalService.propose(command)

        return ProposalResponse.Propose(proposalId = proposal.proposalId)
    }
}