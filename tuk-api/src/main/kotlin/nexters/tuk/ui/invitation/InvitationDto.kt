package nexters.tuk.ui.invitation

import nexters.tuk.application.invitation.dto.request.InvitationCommand
import nexters.tuk.application.invitation.vo.InvitationPurpose

class InvitationDto {
    class Request {
        data class Publish(
            val gatheringId: Long,
            val purpose: InvitationPurpose,
        ) {
            fun toCommand(memberId: Long): InvitationCommand.Publish {
                return InvitationCommand.Publish(
                    memberId = memberId,
                    gatheringId = gatheringId,
                    purpose = purpose
                )
            }
        }
    }
}