package nexters.tuk.application.onboarding

import nexters.tuk.application.onboarding.dto.request.OnboardingCommand
import nexters.tuk.application.onboarding.dto.response.OnboardingResponse
import nexters.tuk.application.onboarding.halper.OnboardingProcessorFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OnboardingService(
    private val onboardingProcessorFactory: OnboardingProcessorFactory,
) {
    @Transactional
    fun initInfo(command: OnboardingCommand.Init): OnboardingResponse.Init {
        OnboardingField.Domain.entries.forEach { domain ->
            val helper = onboardingProcessorFactory.getHelper(domain)
            helper.update(command)
        }

        return OnboardingResponse.Init(memberId = command.memberId)
    }

    @Transactional(readOnly = true)
    fun getRequiredFields(memberId: Long): OnboardingResponse.RequiredFields {
        val fields = buildList {
            OnboardingField.Domain.entries.forEach { domain ->
                val helper = onboardingProcessorFactory.getHelper(domain)

                addAll(helper.requiredInitFields(memberId))
            }
        }

        return OnboardingResponse.RequiredFields(fields)
    }
}