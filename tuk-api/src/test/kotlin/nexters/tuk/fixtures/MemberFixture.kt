package nexters.tuk.fixtures

import nexters.tuk.application.member.SocialType
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository

object MemberFixture {
    fun memberSignUpCommand(
        socialId: String = "1",
        socialType: SocialType = SocialType.GOOGLE,
        email: String = "test@test.com"
    ) = MemberCommand.Login(
        socialId = socialId,
        socialType = socialType,
        email = email
    )
}

class MemberFixtureHelper(
    private val memberRepository: MemberRepository
) {
    fun createMember(
        socialId: String = "1",
        email: String = "test@test.com",
        name: String = "테스트사용자"
    ): Member {
        val member = memberRepository.save(
            Member.signUp(MemberFixture.memberSignUpCommand(socialId = socialId, email = email))
        )
        member.setInitialProfile(MemberCommand.Onboarding(member.id, name))
        return memberRepository.save(member)
    }

    fun createMembers(count: Int, prefix: String = "member"): List<Member> {
        return (1..count).map { index ->
            createMember(
                socialId = "${prefix}${index}",
                email = "${prefix}${index}@test.com",
                name = "${prefix}${index}"
            )
        }
    }
}