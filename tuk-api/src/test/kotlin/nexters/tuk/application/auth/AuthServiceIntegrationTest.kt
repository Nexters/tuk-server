package nexters.tuk.application.auth

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import nexters.tuk.application.auth.dto.request.AuthCommand
import nexters.tuk.application.member.SocialType
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.auth.JwtRepository
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.fixtures.MemberFixture
import nexters.tuk.fixtures.MemberFixtureHelper
import nexters.tuk.testcontainers.RedisCleanUp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AuthServiceIntegrationTest @Autowired constructor(
    private val authService: AuthService,
    private val memberRepository: MemberRepository,
    private val jwtRepository: JwtRepository,
    private val redisCleanUp: RedisCleanUp,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)

    @MockkBean
    private lateinit var socialUserProviderFactory: SocialUserProviderFactory

    @MockkBean
    private lateinit var googleProvider: SocialUserProvider.Google

    @MockkBean
    private lateinit var appleProvider: SocialUserProvider.Apple

    @AfterEach
    fun tearDown() {
        memberRepository.deleteAllInBatch()
        redisCleanUp.flushAll()
    }

    @Test
    fun `Google 소셜 로그인 성공시 기존 회원 정보를 반환한다`() {
        // given
        val command = AuthCommand.SocialLogin.Google("google-id-token", "device-id")
        val socialUserInfo = SocialUserInfo("google-123", SocialType.GOOGLE, "test@example.com")

        // 기존 회원 생성 (이름 초기화 안됨)
        val existingMember = memberRepository.save(
            Member.signUp(
                MemberFixture.memberSignUpCommand(
                    socialId = "google-123",
                    email = "test@example.com"
                )
            )
        )

        every { socialUserProviderFactory.getProvider(command) } returns googleProvider
        every { googleProvider.getSocialUser(command) } returns socialUserInfo

        // when
        val result = authService.socialLogin(command)

        // then
        assertThat(result.memberId).isEqualTo(existingMember.id)
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
        assertThat(result.requiredOnboardingData).containsExactly("NAME")

        // Redis에 refresh token이 저장되었는지 확인
        val savedToken = jwtRepository.findRefreshTokenById(existingMember.id)
        assertThat(savedToken).isEqualTo(result.refreshToken)
    }

    @Test
    fun `Google 소셜 로그인시 신규 회원이면 회원가입 후 토큰을 발급한다`() {
        // given
        val command = AuthCommand.SocialLogin.Google("google-id-token", "device-id")
        val socialUserInfo = SocialUserInfo("google-new-123", SocialType.GOOGLE, "new@example.com")

        every { socialUserProviderFactory.getProvider(command) } returns googleProvider
        every { googleProvider.getSocialUser(command) } returns socialUserInfo

        // when
        val result = authService.socialLogin(command)

        // then
        assertThat(result.memberId).isPositive()
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
        assertThat(result.requiredOnboardingData).containsExactly("NAME")

        // 신규 회원이 저장되었는지 확인
        val savedMember = memberRepository.findById(result.memberId).orElse(null)
        assertThat(savedMember).isNotNull
        assertThat(savedMember.email).isEqualTo("new@example.com")
        assertThat(savedMember.socialType).isEqualTo(SocialType.GOOGLE)
        assertThat(savedMember.socialId).isEqualTo("google-new-123")

        // Redis에 refresh token이 저장되었는지 확인
        val savedToken = jwtRepository.findRefreshTokenById(result.memberId)
        assertThat(savedToken).isEqualTo(result.refreshToken)
    }

    @Test
    fun `Apple 소셜 로그인 성공시 기존 회원 정보를 반환한다`() {
        // given
        val command = AuthCommand.SocialLogin.Apple("apple-id-token")
        val socialUserInfo = SocialUserInfo("apple-123", SocialType.APPLE, "test@icloud.com")

        // 기존 회원 생성 (Apple 타입, 이름 초기화 안됨)
        val existingMember = memberRepository.save(
            Member.signUp(
                MemberFixture.memberSignUpCommand(
                    socialId = "apple-123",
                    socialType = SocialType.APPLE,
                    email = "test@icloud.com"
                )
            )
        )

        every { socialUserProviderFactory.getProvider(command) } returns appleProvider
        every { appleProvider.getSocialUser(command) } returns socialUserInfo

        // when
        val result = authService.socialLogin(command)

        // then
        assertThat(result.memberId).isEqualTo(existingMember.id)
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
        assertThat(result.requiredOnboardingData).containsExactly("NAME")

        // Redis에 refresh token이 저장되었는지 확인
        val savedToken = jwtRepository.findRefreshTokenById(existingMember.id)
        assertThat(savedToken).isEqualTo(result.refreshToken)
    }

    @Test
    fun `유효한 리프레시 토큰으로 액세스 토큰 갱신에 성공한다`() {
        // given
        // 기존 회원 생성
        val member = memberFixture.createMember(
            socialId = "google-123",
            email = "test@example.com"
        )

        // 소셜 로그인을 통해 초기 토큰 발급
        val command = AuthCommand.SocialLogin.Google("google-id-token", "device-id")
        val socialUserInfo = SocialUserInfo("google-123", SocialType.GOOGLE, "test@example.com")

        every { socialUserProviderFactory.getProvider(command) } returns googleProvider
        every { googleProvider.getSocialUser(command) } returns socialUserInfo

        val loginResult = authService.socialLogin(command)
        val refreshCommand = AuthCommand.Refresh(loginResult.refreshToken)

        // 토큰 생성 시간이 달라지도록 지연
        Thread.sleep(1000)

        // when
        val result = authService.refreshAccessToken(refreshCommand)

        // then
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
        assertThat(result.accessToken).isNotEqualTo(loginResult.accessToken)
        assertThat(result.refreshToken).isNotEqualTo(loginResult.refreshToken)

        // 새로운 refresh token이 Redis에 저장되었는지 확인
        val savedToken = jwtRepository.findRefreshTokenById(member.id)
        assertThat(savedToken).isEqualTo(result.refreshToken)
    }

    @Test
    fun `저장되지 않은 리프레시 토큰으로 갱신 시도시 예외가 발생한다`() {
        // given
        // 유효한 JWT이지만 Redis에 저장되지 않은 토큰 생성
        val member = memberFixture.createMember(
            socialId = "google-123",
            email = "test@example.com"
        )

        // 임의의 유효한 refresh token 생성 (하지만 Redis에는 저장되지 않음)
        val fakeJwt = JwtProvider(
            "myVeryLongSecretKeyThatIsAtLeast32CharactersLongForJWT", 30L, 90L
        ).generateTokens(member.id.toString())

        val refreshCommand = AuthCommand.Refresh(fakeJwt.refreshToken)

        // when & then
        val exception = assertThrows<BaseException> {
            authService.refreshAccessToken(refreshCommand)
        }

        assertThat(exception.errorType).isEqualTo(ErrorType.BAD_REQUEST)
        assertThat(exception.message).isEqualTo("유효하지 않은 토큰입니다.")
    }

    @Test
    fun `Redis에서 삭제된 리프레시 토큰으로 갱신 시도시 예외가 발생한다`() {
        // given
        // 회원 생성 및 로그인
        val member = memberFixture.createMember(
            socialId = "google-123",
            email = "test@example.com"
        )

        val command = AuthCommand.SocialLogin.Google("google-id-token", "device-id")
        val socialUserInfo = SocialUserInfo("google-123", SocialType.GOOGLE, "test@example.com")

        every { socialUserProviderFactory.getProvider(command) } returns googleProvider
        every { googleProvider.getSocialUser(command) } returns socialUserInfo

        val loginResult = authService.socialLogin(command)

        // Redis에서 리프레시 토큰 삭제 (로그아웃 등의 상황)
        redisCleanUp.flushAll()

        val refreshCommand = AuthCommand.Refresh(loginResult.refreshToken)

        // when & then
        val exception = assertThrows<BaseException> {
            authService.refreshAccessToken(refreshCommand)
        }

        assertThat(exception.errorType).isEqualTo(ErrorType.BAD_REQUEST)
        assertThat(exception.message).isEqualTo("유효하지 않은 토큰입니다.")
    }

    @Test
    fun `잘못된 형식의 리프레시 토큰으로 갱신 시도시 예외가 발생한다`() {
        // given
        val refreshCommand = AuthCommand.Refresh("invalid-jwt-token")

        // when & then
        val exception = assertThrows<BaseException> {
            authService.refreshAccessToken(refreshCommand)
        }

        assertThat(exception.errorType).isEqualTo(ErrorType.UNAUTHORIZED)
        assertThat(exception.message).isEqualTo("인증에 실패했습니다.")
    }

    @Test
    fun `첫 로그인 시 requiredOnboardingData에 NAME이 포함된다`() {
        // given
        val command = AuthCommand.SocialLogin.Google("google-id-token", "device-id")
        val socialUserInfo = SocialUserInfo("google-first-login", SocialType.GOOGLE, "firstlogin@example.com")

        every { socialUserProviderFactory.getProvider(command) } returns googleProvider
        every { googleProvider.getSocialUser(command) } returns socialUserInfo

        // when
        val result = authService.socialLogin(command)

        // then
        assertThat(result.memberId).isPositive()
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
        assertThat(result.requiredOnboardingData).containsExactly("NAME")

        // 생성된 회원의 name이 초기화되지 않았는지 확인
        val savedMember = memberRepository.findById(result.memberId).orElse(null)
        assertThat(savedMember).isNotNull
        assertThat(savedMember.getRequiredOnboardingData()).containsExactly("NAME")
    }

    @Test
    fun `온보딩 완료 후 로그인시 requiredOnboardingData가 비어있다`() {
        // given
        val member = memberFixture.createMember(
            socialId = "google-123",
            email = "test@example.com"
        )

        // 온보딩 완료
        member.updateProfile(
            nexters.tuk.application.member.dto.request.MemberCommand.Onboarding(
                memberId = member.id,
                name = "홍길동"
            )
        )
        memberRepository.save(member)

        val command = AuthCommand.SocialLogin.Google("google-id-token", "device-id")
        val socialUserInfo = SocialUserInfo("google-123", SocialType.GOOGLE, "test@example.com")

        every { socialUserProviderFactory.getProvider(command) } returns googleProvider
        every { googleProvider.getSocialUser(command) } returns socialUserInfo

        // when
        val result = authService.socialLogin(command)

        // then
        assertThat(result.memberId).isEqualTo(member.id)
        assertThat(result.accessToken).isNotBlank()
        assertThat(result.refreshToken).isNotBlank()
        assertThat(result.requiredOnboardingData).isEmpty()
    }
}