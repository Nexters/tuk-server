package nexters.tuk.application.onboarding.halper

import nexters.tuk.application.member.MemberService
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.application.member.dto.response.MemberResponse
import nexters.tuk.application.onboarding.OnboardingField
import nexters.tuk.application.onboarding.dto.request.OnboardingCommand
import org.springframework.stereotype.Component

@Component
class MemberOnboardingProcessor(
    private val memberService: MemberService
) : OnboardingProcessor<MemberResponse.Profile>() {
    override val domain = OnboardingField.Domain.MEMBER

    override fun getIncompleteOnboardingData(memberId: Long): MemberResponse.Profile {
        return memberService.getMemberProfile(memberId)
    }

    override fun validate(command: OnboardingCommand.Init, data: MemberResponse.Profile) {
        val memberCommand = command.memberInit

        require(!(memberCommand.name.isNullOrBlank() && data.name.isNullOrBlank())) { "사용자 이름을 입력하세요" }
    }

    override fun patchNonNullFields(command: OnboardingCommand.Init, data: MemberResponse.Profile) {
        val memberCommand = command.memberInit

        memberService.updateProfile(
            MemberCommand.UpdateProfile(
                memberId = command.memberId,
                name = memberCommand.name.takeIf { data.name.isNullOrBlank() }
            )
        )
    }

    override fun requiredInitFields(memberId: Long): List<OnboardingField> {
        val member = memberService.getMemberProfile(memberId)

        return buildList {
            if (member.name.isNullOrBlank()) add(OnboardingField.MEMBER_NAME)
        }
    }
}