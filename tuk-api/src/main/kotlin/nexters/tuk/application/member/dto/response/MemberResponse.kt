package nexters.tuk.application.member.dto.response

import nexters.tuk.application.member.SocialType

class MemberResponse {
    data class SignUp(
        val memberId: Long,
        val email: String,
        val socialType: SocialType,
        val socialId: String,
    )
}