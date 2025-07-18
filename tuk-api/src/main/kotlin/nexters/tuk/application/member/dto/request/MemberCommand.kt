package nexters.tuk.application.member.dto.request

import nexters.tuk.application.member.SocialType

class MemberCommand {
    data class SignUp(
        val email: String,
        val socialId: String,
        val socialType: SocialType,
    )
}