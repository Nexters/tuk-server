package nexters.tuk.application.onboarding

import nexters.tuk.application.onboarding.dto.request.OnboardingCommand
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
class OnboardingServiceIntegrationTest @Autowired constructor(
    private val onboardingService: OnboardingService,
    private val memberRepository: MemberRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)

    @AfterEach
    fun tearDown() {
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `온보딩 정보 초기화 성공 - DB에 이름이 없고 입력값이 있는 경우`() {
        // given
        val member = memberRepository.save(
            Member.signUp(
                MemberFixture.memberSignUpCommand(
                    socialId = "google-123",
                    email = "test@example.com"
                )
            )
        )
        
        val command = OnboardingCommand.Init(
            memberId = member.id,
            memberInit = OnboardingCommand.Init.MemberInit(
                name = "홍길동"
            )
        )

        // when
        val result = onboardingService.initInfo(command)

        // then
        assertThat(result.memberId).isEqualTo(member.id)
        
        // DB에 이름이 업데이트되었는지 확인
        val updatedMember = memberRepository.findById(member.id).orElse(null)
        assertThat(updatedMember).isNotNull
        assertThat(updatedMember.name).isEqualTo("홍길동")
    }

    @Test
    fun `온보딩 정보 초기화 실패 - DB에 이름이 없고 입력값도 없는 경우 예외 발생`() {
        // given
        val member = memberRepository.save(
            Member.signUp(
                MemberFixture.memberSignUpCommand(
                    socialId = "google-123",
                    email = "test@example.com"
                )
            )
        )
        
        val command = OnboardingCommand.Init(
            memberId = member.id,
            memberInit = OnboardingCommand.Init.MemberInit(
                name = null
            )
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            onboardingService.initInfo(command)
        }
        
        assertThat(exception.message).isEqualTo("사용자 이름을 입력하세요")
    }

    @Test
    fun `온보딩 정보 초기화 성공 - DB에 이미 이름이 있는 경우 업데이트하지 않음`() {
        // given
        val member = memberFixture.createMember(
            socialId = "google-123",
            email = "test@example.com"
        )
        
        // 기존에 이름이 설정된 상태
        member.updateProfile("기존이름")
        memberRepository.save(member)
        
        val command = OnboardingCommand.Init(
            memberId = member.id,
            memberInit = OnboardingCommand.Init.MemberInit(
                name = "새이름"
            )
        )

        // when
        val result = onboardingService.initInfo(command)

        // then
        assertThat(result.memberId).isEqualTo(member.id)
        
        // DB에 기존 이름이 유지되는지 확인 (takeIf 조건으로 업데이트되지 않음)
        val updatedMember = memberRepository.findById(member.id).orElse(null)
        assertThat(updatedMember).isNotNull
        assertThat(updatedMember.name).isEqualTo("기존이름")
    }

    @Test
    fun `필수 필드 조회 - 이름이 없는 경우 MEMBER_NAME 반환`() {
        // given
        val member = memberRepository.save(
            Member.signUp(
                MemberFixture.memberSignUpCommand(
                    socialId = "google-123",
                    email = "test@example.com"
                )
            )
        )

        // when
        val result = onboardingService.getRequiredFields(member.id)

        // then
        assertThat(result.fields).containsExactly(OnboardingField.MEMBER_NAME)
    }

    @Test
    fun `필수 필드 조회 - 이름이 있는 경우 빈 리스트 반환`() {
        // given
        val member = memberFixture.createMember(
            socialId = "google-123",
            email = "test@example.com"
        )
        
        // 이름 설정
        member.updateProfile("홍길동")
        memberRepository.save(member)

        // when
        val result = onboardingService.getRequiredFields(member.id)

        // then
        assertThat(result.fields).isEmpty()
    }

    @Test
    fun `온보딩 정보 초기화 실패 - 빈 문자열도 null과 동일하게 처리하여 예외 발생`() {
        // given
        val member = memberRepository.save(
            Member.signUp(
                MemberFixture.memberSignUpCommand(
                    socialId = "google-123",
                    email = "test@example.com"
                )
            )
        )
        
        val command = OnboardingCommand.Init(
            memberId = member.id,
            memberInit = OnboardingCommand.Init.MemberInit(
                name = "   "  // 공백문자
            )
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            onboardingService.initInfo(command)
        }
        
        assertThat(exception.message).isEqualTo("사용자 이름을 입력하세요")
    }
}