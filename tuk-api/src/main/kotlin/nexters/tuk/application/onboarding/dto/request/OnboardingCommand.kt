package nexters.tuk.application.onboarding.dto.request


class OnboardingCommand {
    data class Init(
        val memberId: Long,
        val memberInit: MemberInit
    ) {
        data class MemberInit(
            val name: String?
        )
    }
}