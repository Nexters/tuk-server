package nexters.tuk.application.member

import jakarta.transaction.Transactional
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.application.member.dto.response.MemberResponse
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {
    @Transactional
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