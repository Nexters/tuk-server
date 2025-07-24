package nexters.tuk.application.invitation

import nexters.tuk.application.invitation.dto.response.InvitationResponse
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import nexters.tuk.domain.invitation.InvitationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InvitationService(
    private val invitationRepository: InvitationRepository,
    private val gatheringRepository: GatheringRepository,
) {
    @Transactional(readOnly = true)
    fun getGatheringInvitationStat(gatheringId: Long, memberId: Long): InvitationResponse.InvitationStat {
        val gathering = gatheringRepository.findByIdOrThrow(gatheringId)
        val invitations = invitationRepository.findByGathering(gathering).toList()

        val sentCount = invitations.count { it.inviterId == memberId }
        val receivedCount = invitations.size - sentCount

        return InvitationResponse.InvitationStat(sentCount, receivedCount)
    }
}