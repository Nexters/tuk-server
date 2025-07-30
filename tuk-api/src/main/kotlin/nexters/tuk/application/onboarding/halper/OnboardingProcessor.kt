package nexters.tuk.application.onboarding.halper

import nexters.tuk.application.onboarding.OnboardingField
import nexters.tuk.application.onboarding.dto.request.OnboardingCommand

abstract class OnboardingProcessor<T> {
    abstract val domain: OnboardingField.Domain

    abstract fun getIncompleteOnboardingData(memberId: Long): T

    abstract fun validate(command: OnboardingCommand.Init, data: T)

    abstract fun patchNonNullFields(command: OnboardingCommand.Init, data: T)

    abstract fun requiredInitFields(memberId: Long): List<OnboardingField>

    fun update(command: OnboardingCommand.Init) {
        val data = getIncompleteOnboardingData(command.memberId)
        validate(command, data)
        patchNonNullFields(command, data)
    }
}