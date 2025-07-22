package nexters.tuk.application.invitation

import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.invitation.Invitation
import nexters.tuk.domain.invitation.InvitationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InvitationService(
    private val invitationRepository: InvitationRepository,
) {
    @Transactional(readOnly = true)
    fun getGatherInvitations(gathering: Gathering): List<Invitation> {
        return invitationRepository.findByGathering(gathering)
    }
}