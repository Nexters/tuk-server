package nexters.tuk.application.auth

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import nexters.tuk.application.auth.dto.request.AuthCommand
import nexters.tuk.application.member.SocialType
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.testcontainers.RedisCleanUp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AuthServiceIntegrationTest @Autowired constructor(
    private val authService: AuthService,
    private val memberRepository: MemberRepository,
    @MockkBean private val socialUserProviderFactory: SocialUserProviderFactory,
    @MockkBean private val googleUserProvider: SocialUserProvider.Google,
    private val redisCleanUp: RedisCleanUp,
) {
    @AfterEach
    fun tearDown() {
        // TODO: DB Cleaner 별도 생성하기
        memberRepository.deleteAll()
        redisCleanUp.flushAll()
    }

    @Test
    fun `소셜 로그인 성공시 유저 정보를 정상적으로 저장한다`() {
        // given
        val command = AuthCommand.SocialLogin.Google(
            idToken = "ABC",
            deviceId = "123"
        )
        val socialUserInfo = SocialUserInfo(
            socialId = "1",
            socialType = SocialType.GOOGLE,
            email = "test@test.com",
        )
        every { socialUserProviderFactory.getProvider(command) } returns googleUserProvider
        every { googleUserProvider.getSocialUser(command) } returns socialUserInfo

        // when
        val actual = authService.socialLogin(command)

        // then
        memberRepository.findByEmail("test@test.com")!!.let { member ->
            assertAll(
                { assertThat(actual).isNotNull },
                { assertThat(member.socialType).isEqualTo(SocialType.GOOGLE) },
                { assertThat(member.email).isEqualTo("test@test.com") },
                { assertThat(member.socialId).isEqualTo("1") }
            )
        }
    }
}