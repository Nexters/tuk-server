package nexters.tuk.application.invitation.dto.response

class InvitationMemberResponse {
    data class PublishedInvitationMembers(
        val invitationMemberIds: List<Long>,
    )
}