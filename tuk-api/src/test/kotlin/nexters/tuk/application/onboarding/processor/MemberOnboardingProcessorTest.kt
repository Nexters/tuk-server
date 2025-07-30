package nexters.tuk.application.onboarding.processor

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.application.member.dto.response.MemberResponse
import nexters.tuk.application.onboarding.OnboardingField
import nexters.tuk.application.onboarding.dto.request.OnboardingCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MemberOnboardingProcessorTest {

    private val memberService = mockk<MemberService>()
    private val processor = MemberOnboardingProcessor(memberService)

    @Test
    fun `도메인이 MEMBER인지 확인`() {
        // when & then
        assertThat(processor.domain).isEqualTo(OnboardingField.Domain.MEMBER)
    }

    @Test
    fun `MEMBER_NAME 필드 처리를 위한 도메인 확인`() {
        // when & then
        assertThat(OnboardingField.MEMBER_NAME.domain).isEqualTo(processor.domain)
    }

    @Test
    fun `회원 프로필 조회가 정상 동작하는지 확인`() {
        // given
        val memberId = 1L
        val memberProfile = MemberResponse.Profile(
            memberId = memberId,
            name = null,
            email = "test@example.com"
        )
        
        every { memberService.getMemberProfile(memberId) } returns memberProfile

        // when
        val result = processor.getIncompleteOnboardingData(memberId)

        // then
        assertThat(result).isEqualTo(memberProfile)
        verify { memberService.getMemberProfile(memberId) }
    }

    @Test
    fun `검증 성공 - 입력값이 있고 DB가 null인 경우`() {
        // given
        val command = OnboardingCommand.Init(
            memberId = 1L,
            memberInit = OnboardingCommand.Init.MemberInit(name = "홍길동")
        )
        val data = MemberResponse.Profile(
            memberId = 1L,
            name = null,
            email = "test@example.com"
        )

        // when & then - 예외가 발생하지 않아야 함
        processor.validate(command, data)
    }

    @Test
    fun `검증 실패 - 입력값도 null이고 DB도 null인 경우`() {
        // given
        val command = OnboardingCommand.Init(
            memberId = 1L,
            memberInit = OnboardingCommand.Init.MemberInit(name = null)
        )
        val data = MemberResponse.Profile(
            memberId = 1L,
            name = null,
            email = "test@example.com"
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            processor.validate(command, data)
        }
        
        assertThat(exception.message).isEqualTo("사용자 이름을 입력하세요")
    }

    @Test
    fun `검증 실패 - 입력값이 빈 문자열이고 DB도 null인 경우`() {
        // given
        val command = OnboardingCommand.Init(
            memberId = 1L,
            memberInit = OnboardingCommand.Init.MemberInit(name = "   ")
        )
        val data = MemberResponse.Profile(
            memberId = 1L,
            name = null,
            email = "test@example.com"
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            processor.validate(command, data)
        }
        
        assertThat(exception.message).isEqualTo("사용자 이름을 입력하세요")
    }

    @Test
    fun `패치 동작 - DB가 null이면 업데이트 실행`() {
        // given
        val command = OnboardingCommand.Init(
            memberId = 1L,
            memberInit = OnboardingCommand.Init.MemberInit(name = "홍길동")
        )
        val data = MemberResponse.Profile(
            memberId = 1L,
            name = null,
            email = "test@example.com"
        )

        every { memberService.updateProfile(any()) } returns mockk()

        // when
        processor.patchNonNullFields(command, data)

        // then
        verify {
            memberService.updateProfile(
                MemberCommand.UpdateProfile(
                    memberId = 1L,
                    name = "홍길동"
                )
            )
        }
    }

    @Test
    fun `패치 동작 - DB에 이미 값이 있으면 업데이트하지 않음`() {
        // given
        val command = OnboardingCommand.Init(
            memberId = 1L,
            memberInit = OnboardingCommand.Init.MemberInit(name = "새이름")
        )
        val data = MemberResponse.Profile(
            memberId = 1L,
            name = "기존이름",
            email = "test@example.com"
        )

        every { memberService.updateProfile(any()) } returns mockk()

        // when
        processor.patchNonNullFields(command, data)

        // then - takeIf 조건으로 null이 전달되어 업데이트되지 않음
        verify {
            memberService.updateProfile(
                MemberCommand.UpdateProfile(
                    memberId = 1L,
                    name = null
                )
            )
        }
    }

    @Test
    fun `필수 필드 조회 - 이름이 null이면 MEMBER_NAME 반환`() {
        // given
        val memberId = 1L
        val memberProfile = MemberResponse.Profile(
            memberId = memberId,
            name = null,
            email = "test@example.com"
        )
        
        every { memberService.getMemberProfile(memberId) } returns memberProfile

        // when
        val result = processor.requiredInitFields(memberId)

        // then
        assertThat(result).containsExactly(OnboardingField.MEMBER_NAME)
    }

    @Test
    fun `필수 필드 조회 - 이름이 있으면 빈 리스트 반환`() {
        // given
        val memberId = 1L
        val memberProfile = MemberResponse.Profile(
            memberId = memberId,
            name = "홍길동",
            email = "test@example.com"
        )
        
        every { memberService.getMemberProfile(memberId) } returns memberProfile

        // when
        val result = processor.requiredInitFields(memberId)

        // then
        assertThat(result).isEmpty()
    }
}