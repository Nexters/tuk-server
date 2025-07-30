package nexters.tuk.application.onboarding.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.onboarding.OnboardingField

class OnboardingResponse {
    @Schema(name = "OnboardingInitResponse")
    data class Init(
        val memberId: Long
    )

    @Schema(name = "OnboardingRequiredFields")
    data class RequiredFields(
        val fields: List<OnboardingField>
    )
}