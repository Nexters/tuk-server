package nexters.tuk.application.onboarding.initializer

import nexters.tuk.application.onboarding.OnboardingField
import org.springframework.stereotype.Component

@Component
class OnboardingInitializerFactory(
    private val processors: List<OnboardingInitializer<*>>
) {
    fun getProcessor(domain: OnboardingField.Domain): OnboardingInitializer<*> {
        return processors.find { it.domain == domain }
            ?: throw IllegalArgumentException("No helper found for domain: $domain")
    }
}