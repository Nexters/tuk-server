package nexters.tuk.ui.onboarding

import nexters.tuk.application.onboarding.dto.request.OnboardingCommand

class OnboardingDto {
    class Request {
        data class Init(
            val name: String
        ) {
            fun toCommand(memberId: Long): OnboardingCommand.Init {
                val memberInit = OnboardingCommand.Init.MemberInit(
                    name = name
                )

                return OnboardingCommand.Init(
                    memberId = memberId,
                    memberInit = memberInit
                )
            }
        }
    }
}