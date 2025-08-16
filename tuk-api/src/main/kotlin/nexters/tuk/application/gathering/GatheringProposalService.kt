package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringProposalCommand
import nexters.tuk.application.proposal.ProposalMemberService
import nexters.tuk.application.proposal.ProposalService
import nexters.tuk.application.push.PushService
import nexters.tuk.application.push.dto.request.PushCommand
import nexters.tuk.contract.push.PushType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringProposalService(
    private val proposalService: ProposalService,
    private val gatheringMemberService: GatheringMemberService,
    private val proposalMemberService: ProposalMemberService,
    private val pushService: PushService,
) {
    @Transactional
    fun addProposal(command: GatheringProposalCommand.AddProposal) {
        gatheringMemberService.verifyGatheringAccess(
            memberId = command.memberId,
            gatheringId = command.gatheringId
        )
        proposalService.addGathering(
            proposalId = command.proposalId,
            gatheringId = command.gatheringId
        )

        val gatheringMembers = gatheringMemberService.getGatheringMemberIds(command.gatheringId)

        proposalMemberService.publishGatheringMembers(
            proposalId = command.proposalId,
            memberIds = gatheringMembers
        )

        pushService.sendPush(
            PushCommand.Push.Proposal(
                pushType = PushType.PROPOSAL,
                gatheringId = command.gatheringId,
                proposalId = command.proposalId,
            )
        )
    }
}