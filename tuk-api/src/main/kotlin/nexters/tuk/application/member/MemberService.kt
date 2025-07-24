package nexters.tuk.application.member

import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.application.member.dto.response.MemberResponse
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {

    @Transactional(readOnly = true)
    fun login(command: MemberCommand.Login): MemberResponse.Login? {
        val member = memberRepository.findBySocialTypeAndSocialId(command.socialType, command.socialId) ?: return null

        return MemberResponse.Login(
            memberId = member.id,
            email = member.email,
            socialType = member.socialType,
            socialId = member.socialId,
        )
    }

    @Transactional
    fun signUp(command: MemberCommand.SignUp): MemberResponse.Login {
        val member = memberRepository.save(Member.signUp(command))

        return MemberResponse.Login(
            memberId = member.id,
            email = member.email,
            socialType = member.socialType,
            socialId = member.socialId,
        )
    }

    @Transactional(readOnly = true)
    fun getMemberOverview(memberIds: List<Long>): List<MemberResponse.MemberOverview> {
        val members = memberRepository.findAllById(memberIds).toList()

        return members.map {
            MemberResponse.MemberOverview(it.id, it.name ?: "이름 없음")
        }
    }
}