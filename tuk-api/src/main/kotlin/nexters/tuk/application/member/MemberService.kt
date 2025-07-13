package nexters.tuk.application.member

import nexters.tuk.application.member.dto.MemberCommand
import nexters.tuk.application.member.dto.MemberResponse
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {
    fun signUp(command: MemberCommand.SignUp): MemberResponse.SignUp {
        val member = memberRepository.save(Member.signUp(command))

        return MemberResponse.SignUp(
            memberId = member.id,
            email = member.email,
            socialType = member.socialType,
            socialId = member.socialId,
        )
    }
}