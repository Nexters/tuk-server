package nexters.tuk.application.member.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.member.SocialType

class MemberResponse {
    data class SignUp(
        val memberId: Long,
        val email: String,
        val socialType: SocialType,
        val socialId: String,
    )

    data class MemberOverview(
        @Schema(description = "사용자 id")
        val memberId: Long,
        @Schema(description = "사용자 명")
        val memberName: String,
    )
}