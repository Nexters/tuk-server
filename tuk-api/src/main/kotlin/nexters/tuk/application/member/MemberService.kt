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
    fun signUp(command: MemberCommand.SignUp): MemberResponse.SignUp {
        val member = memberRepository.save(Member.signUp(command))

        return MemberResponse.SignUp(
            memberId = member.id,
            email = member.email,
            socialType = member.socialType,
            socialId = member.socialId,
        )
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Member {
        return memberRepository.findById(id)
            .orElseThrow { throw BaseException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다.") }
    }
}