package nexters.tuk.application.member.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.member.SocialType

class MemberResponse {
    data class Login(
        val memberId: Long,
        val email: String,
        val socialType: SocialType,
        val socialId: String,
        val requiredOnboardingData: List<String>
    )

    data class Overview(
        @Schema(description = "사용자 id")
        val memberId: Long,
        @Schema(description = "사용자 명")
        val memberName: String,
    )

    @Schema(name = "OnboardingResponse")
    data class Onboarding(
        val memberId: Long,
        val name: String
    )
}