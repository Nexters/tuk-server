package nexters.tuk.application.member

import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.fixtures.MemberFixture
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MemberServiceIntegrationTest @Autowired constructor(
    private val memberService: MemberService,
    private val memberRepository: MemberRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)

    @AfterEach
    fun tearDown() {
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `온보딩 성공시 사용자 정보가 업데이트된다`() {
        // given
        val member = memberFixture.createMember(
            socialId = "google-123",
            email = "test@example.com"
        )

        val command = MemberCommand.Onboarding(
            memberId = member.id,
            name = "홍길동"
        )

        // when
        val result = memberService.executeOnboarding(command)

        // then
        assertThat(result.memberId).isEqualTo(member.id)
        assertThat(result.name).isEqualTo("홍길동")

        // DB에서 실제로 업데이트되었는지 확인
        val updatedMember = memberRepository.findById(member.id).orElse(null)
        assertThat(updatedMember).isNotNull
        assertThat(updatedMember.name).isEqualTo("홍길동")
        assertThat(updatedMember.getRequiredOnboardingData()).isEmpty()
    }

    @Test
    fun `존재하지 않는 사용자로 온보딩 시도시 예외가 발생한다`() {
        // given
        val command = MemberCommand.Onboarding(
            memberId = 999L,
            name = "홍길동"
        )

        // when & then
        val exception = assertThrows<BaseException> {
            memberService.executeOnboarding(command)
        }

        assertThat(exception.errorType).isEqualTo(ErrorType.NOT_FOUND)
        assertThat(exception.message).isEqualTo("찾을 수 없는 사용자 입니다.")
    }

    @Test
    fun `빈 이름으로 온보딩 시도시 예외가 발생한다`() {
        // given
        val member = memberFixture.createMember(
            socialId = "google-123",
            email = "test@example.com"
        )

        val command = MemberCommand.Onboarding(
            memberId = member.id,
            name = ""
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            memberService.executeOnboarding(command)
        }

        assertThat(exception.message).isEqualTo("이름은 필수 입니다.")
    }

    @Test
    fun `공백만 있는 이름으로 온보딩 시도시 예외가 발생한다`() {
        // given
        val member = memberFixture.createMember(
            socialId = "google-123",
            email = "test@example.com"
        )

        val command = MemberCommand.Onboarding(
            memberId = member.id,
            name = "   "
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            memberService.executeOnboarding(command)
        }

        assertThat(exception.message).isEqualTo("이름은 필수 입니다.")
    }

    @Test
    fun `여러 사용자 개요 조회가 성공한다`() {
        // given
        val member1 = memberRepository.save(
            Member.signUp(
                MemberFixture.memberSignUpCommand(
                    socialId = "google-123",
                    email = "test1@example.com"
                )
            )
        )
        member1.updateProfile(MemberCommand.Onboarding(member1.id, "사용자1"))

        val member2 = memberRepository.save(
            Member.signUp(
                MemberFixture.memberSignUpCommand(
                    socialId = "google-456",
                    email = "test2@example.com"
                )
            )
        )
        member2.updateProfile(MemberCommand.Onboarding(member2.id, "사용자2"))

        memberRepository.saveAll(listOf(member1, member2))

        // when
        val result = memberService.getMemberOverviews(listOf(member1.id, member2.id))

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].memberId).isEqualTo(member1.id)
        assertThat(result[0].memberName).isEqualTo("사용자1")
        assertThat(result[1].memberId).isEqualTo(member2.id)
        assertThat(result[1].memberName).isEqualTo("사용자2")
    }

    @Test
    fun `존재하지 않는 사용자 ID가 포함된 개요 조회시 해당 사용자는 제외된다`() {
        // given
        val member = memberFixture.createMember(
            socialId = "google-123",
            email = "test@example.com"
        )
        member.updateProfile(MemberCommand.Onboarding(member.id, "사용자1"))
        memberRepository.save(member)

        // when
        val result = memberService.getMemberOverviews(listOf(member.id, 999L))

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].memberId).isEqualTo(member.id)
        assertThat(result[0].memberName).isEqualTo("사용자1")
    }
}