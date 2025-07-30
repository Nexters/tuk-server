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
            memberRepository.findBySocialTypeAndSocialId(command.socialType, command.socialId)
                ?: memberRepository.save(Member.signUp(command))

        return MemberResponse.Login(
            memberId = member.id,
            email = member.email,
            socialType = member.socialType,
            socialId = member.socialId,
            memberName = member.name,
        )
    }

    @Transactional(readOnly = true)
    fun getMemberProfile(id: Long): MemberResponse.Profile {
        val member = memberRepository.findById(id)
            .orElseThrow { BaseException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.") }

        return MemberResponse.Profile(
            memberId = member.id,
            email = member.email,
            name = member.name
        )
    }

    @Transactional(readOnly = true)
    fun getMemberOverviews(memberIds: List<Long>): List<MemberResponse.Overview> {
        val members = memberRepository.findAllById(memberIds).toList()

        return members
            .map {
                MemberResponse.Overview(
                    memberId = it.id,
                    memberName = it.name ?: "이름 없음",
                )
            }
    }

    @Transactional
    fun updateProfile(command: MemberCommand.UpdateProfile): MemberResponse.Profile {
        val member = memberRepository.findById(command.memberId)
            .orElseThrow { BaseException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.") }

        member.updateProfile(command.name)

        return MemberResponse.Profile(
            memberId = member.id,
            email = member.email,
            name = member.name,
        )
    }
}