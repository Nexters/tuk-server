package nexters.tuk.application.onboarding.dto.response

import nexters.tuk.application.onboarding.OnboardingField

class OnboardingResponse {
    data class Init(
        val memberId: Long
    )

    data class RequiredFields(
        val fields: List<OnboardingField>
    )
}