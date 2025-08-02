package nexters.tuk.application.invitation

import nexters.tuk.application.gathering.GatheringMemberService
import nexters.tuk.application.invitation.dto.request.InvitationCommand
import nexters.tuk.application.invitation.dto.response.InvitationResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InvitationGenerateService(
    private val invitationService: InvitationService,
    private val invitationMemberService: InvitationMemberService,
    private val gatheringMemberService: GatheringMemberService,
) {
    @Transactional
    fun publishInvitation(command: InvitationCommand.Publish): InvitationResponse.Publish {
        gatheringMemberService.verifyGatheringAccess(
            memberId = command.memberId,
            gatheringId = command.gatheringId
        )
        val invitation = invitationService.publishInvitation(command)
        val gatheringMembers = gatheringMemberService.getGatheringMemberIds(command.gatheringId)

        invitationMemberService.publishGatheringMembers(invitation.invitationId, gatheringMembers)

        return InvitationResponse.Publish(invitationId = invitation.invitationId)
    }
}