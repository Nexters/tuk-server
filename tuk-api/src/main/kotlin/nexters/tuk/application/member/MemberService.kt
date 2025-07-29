package nexters.tuk.application.member

import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.application.member.dto.response.MemberResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {

    @Transactional
    fun login(command: MemberCommand.Login): MemberResponse.Login {
        val member =
            memberRepository.findBySocialTypeAndSocialId(command.socialType, command.socialId) ?: memberRepository.save(
                Member.signUp(command)
            )

        return MemberResponse.Login(
            memberId = member.id,
            email = member.email,
            socialType = member.socialType,
            socialId = member.socialId,
            requiredOnboardingData = member.getRequiredOnboardingData(),
        )
    }

    @Transactional(readOnly = true)
    fun getMemberOverviews(memberIds: List<Long>): List<MemberResponse.Overview> {
        val members = memberRepository.findAllById(memberIds).toList()

        return members
            .map {
                MemberResponse.Overview(
                    memberId = it.id,
                    memberName = it.name
                )
            }
    }

    @Transactional
    fun executeOnboarding(command: MemberCommand.Onboarding): MemberResponse.Onboarding {
        val member = memberRepository.findById(command.memberId).orElseThrow {
            BaseException(ErrorType.NOT_FOUND, "찾을 수 없는 사용자 입니다.")
        }
        member.setInitialProfile(command)

        return MemberResponse.Onboarding(
            memberId = member.id,
            name = member.name
        )
    }
}