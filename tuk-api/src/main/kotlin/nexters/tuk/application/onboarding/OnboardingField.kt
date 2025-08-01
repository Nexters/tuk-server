package nexters.tuk.application.onboarding

enum class OnboardingField(val domain: Domain) {
    MEMBER_NAME(Domain.MEMBER);

    enum class Domain {
        MEMBER,
    }
}