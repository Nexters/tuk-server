package nexters.tuk.application.onboarding.processor

import nexters.tuk.application.onboarding.OnboardingField
import org.springframework.stereotype.Component

@Component
class OnboardingProcessorFactory(
    private val processors: List<OnboardingProcessor<*>>
) {
    fun getProcessor(domain: OnboardingField.Domain): OnboardingProcessor<*> {
        return processors.find { it.domain == domain }
            ?: throw IllegalArgumentException("No helper found for domain: $domain")
    }
}