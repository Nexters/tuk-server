package nexters.tuk.ui.invitation

import nexters.tuk.application.invitation.InvitationGenerateService
import nexters.tuk.application.invitation.dto.response.InvitationResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/invitations")
class InvitationController(
    private val invitationGenerateService: InvitationGenerateService,
) : InvitationSpec {

    @PostMapping
    override fun generateInvitation(
        @Authenticated memberId: Long,
        request: InvitationDto.Request.Publish
    ): ApiResponse<InvitationResponse.Publish> {
        val response = invitationGenerateService.publishInvitation(request.toCommand(memberId))

        return ApiResponse.ok(response)
    }
}