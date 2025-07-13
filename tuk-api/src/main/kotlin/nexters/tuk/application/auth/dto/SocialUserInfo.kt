package nexters.tuk.application.auth.dto

import nexters.tuk.application.member.SocialType

data class SocialUserInfo(
    val socialId: String,
    val socialType: SocialType,
    val email: String,
)