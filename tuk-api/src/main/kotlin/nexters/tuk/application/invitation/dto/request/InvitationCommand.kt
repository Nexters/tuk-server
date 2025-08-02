package nexters.tuk.application.invitation.dto.request

import nexters.tuk.application.invitation.vo.InvitationPurpose

class InvitationCommand {
    data class Publish(
        val memberId: Long,
        val gatheringId: Long,
        val purpose: InvitationPurpose
    )
}