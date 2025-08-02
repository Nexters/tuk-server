package nexters.tuk.application.invitation

import nexters.tuk.application.invitation.dto.request.InvitationCommand
import nexters.tuk.application.invitation.dto.response.InvitationResponse
import nexters.tuk.domain.invitation.Invitation
import nexters.tuk.domain.invitation.InvitationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InvitationService(
    private val invitationRepository: InvitationRepository,
) {
    @Transactional(readOnly = true)
    fun getGatheringInvitationStat(
        gatheringId: Long,
        memberId: Long
    ): InvitationResponse.InvitationStat {
        val invitations = invitationRepository.findByGatheringId(gatheringId)

        val sentCount = invitations.count { it.inviterId == memberId }
        val receivedCount = invitations.count { it.inviterId != memberId }

        return InvitationResponse.InvitationStat(sentCount, receivedCount)
    }

    @Transactional
    fun publishInvitation(command: InvitationCommand.Publish): InvitationResponse.Publish {
        val invitation = Invitation.publish(
            gatheringId = command.gatheringId,
            inviterId = command.memberId,
            purpose = command.purpose.toString(),
        ).let { invitationRepository.save(it) }

        return InvitationResponse.Publish(invitationId = invitation.id)
    }
}