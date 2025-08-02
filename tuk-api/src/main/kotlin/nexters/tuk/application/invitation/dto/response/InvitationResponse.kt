package nexters.tuk.application.invitation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

class InvitationResponse {
    data class InvitationStat(
        @Schema(description = "보낸 초대장 수")
        val sentCount: Int,
        @Schema(description = "받은 초대장 수")
        val receivedCount: Int,
    )

    data class Publish(
        val invitationId: Long,
    )
}