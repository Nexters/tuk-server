package nexters.tuk.application.member.dto.request

import nexters.tuk.application.member.SocialType

class MemberCommand {
    data class Login(
        val email: String,
        val socialId: String,
        val socialType: SocialType,
    )

    data class UpdateProfile(
        val memberId: Long,
        val name: String?,
    )
}