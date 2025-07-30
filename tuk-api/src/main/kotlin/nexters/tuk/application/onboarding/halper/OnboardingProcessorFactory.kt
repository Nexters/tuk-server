package nexters.tuk.application.onboarding.halper

import nexters.tuk.application.onboarding.OnboardingField
import org.springframework.stereotype.Component

@Component
class OnboardingProcessorFactory(
    private val helpers: List<OnboardingProcessor<*>>
) {
    fun getHelper(domain: OnboardingField.Domain): OnboardingProcessor<*> {
        return helpers.find { it.domain == domain }
            ?: throw IllegalArgumentException("No helper found for domain: $domain")
    }
}