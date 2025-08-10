package nexters.tuk.application.proposal

import nexters.tuk.application.gathering.GatheringMemberService
import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.application.push.PushService
import nexters.tuk.application.push.dto.request.PushCommand
import nexters.tuk.contract.push.PushType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProposalCreateService(
    private val proposalService: ProposalService,
    private val proposalMemberService: ProposalMemberService,
    private val gatheringMemberService: GatheringMemberService,
    private val pushService: PushService,
) {
    @Transactional
    fun propose(command: ProposalCommand.Propose): ProposalResponse.Propose {
        gatheringMemberService.verifyGatheringAccess(
            memberId = command.memberId,
            gatheringId = command.gatheringId
        )
        val proposal = proposalService.propose(command)
        val gatheringMembers = gatheringMemberService.getGatheringMemberIds(command.gatheringId)

        proposalMemberService.publishGatheringMembers(
            proposalId = proposal.proposalId,
            memberIds = gatheringMembers
        )

        pushService.sendPush(
            PushCommand.Push.Proposal(
                pushType = PushType.PROPOSAL,
                gatheringId = command.gatheringId
            )
        )
        return ProposalResponse.Propose(proposalId = proposal.proposalId)
    }
}