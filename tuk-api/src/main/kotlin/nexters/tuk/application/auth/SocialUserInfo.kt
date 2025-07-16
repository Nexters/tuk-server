package nexters.tuk.application.auth

import nexters.tuk.application.member.SocialType

data class SocialUserInfo(
    val socialId: String,
    val socialType: SocialType,
    val email: String,
)