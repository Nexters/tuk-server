package nexters.tuk.application.proposal

import nexters.tuk.application.gathering.GatheringMemberService
import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProposalCreateService(
    private val proposalService: ProposalService,
    private val proposalMemberService: ProposalMemberService,
    private val gatheringMemberService: GatheringMemberService,
) {
    @Transactional
    fun propose(command: ProposalCommand.Propose): ProposalResponse.Propose {
        gatheringMemberService.verifyGatheringAccess(
            memberId = command.memberId,
            gatheringId = command.gatheringId
        )
        val proposal = proposalService.propose(command)
        val gatheringMembers = gatheringMemberService.getGatheringMemberIds(command.gatheringId)

        proposalMemberService.publishGatheringMembers(proposal.proposalId, gatheringMembers)

        return ProposalResponse.Propose(proposalId = proposal.proposalId)
    }
}