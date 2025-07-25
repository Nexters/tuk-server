package nexters.tuk.application.auth

import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.testcontainers.RedisCleanUp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class JwtProviderIntegrationTest @Autowired constructor(
    private val jwtProvider: JwtProvider,
    private val redisCleanUp: RedisCleanUp,
) {

    @AfterEach
    fun tearDown() {
        redisCleanUp.flushAll()
    }

    @Test
    fun `JWT 토큰 생성시 액세스 토큰과 리프레시 토큰이 정상적으로 생성된다`() {
        // given
        val memberId = "123"

        // when
        val jwt = jwtProvider.generateTokens(memberId)

        // then
        assertThat(jwt.accessToken).isNotBlank()
        assertThat(jwt.refreshToken).isNotBlank()
        assertThat(jwt.refreshExpiresIn).isPositive()
        assertThat(jwt.accessToken).isNotEqualTo(jwt.refreshToken)
    }

    @Test
    fun `생성된 액세스 토큰이 올바른 구조와 클레임을 가진다`() {
        // given
        val memberId = "456"

        // when
        val jwt = jwtProvider.generateTokens(memberId)

        // then
        // 액세스 토큰 검증을 통해 구조 확인
        val extractedMemberId = jwtProvider.validateAccessTokenAndGetMemberId(jwt.accessToken)
        assertThat(extractedMemberId).isEqualTo(456L)
    }

    @Test
    fun `생성된 리프레시 토큰이 올바른 구조와 클레임을 가진다`() {
        // given
        val memberId = "789"

        // when
        val jwt = jwtProvider.generateTokens(memberId)

        // then
        // 리프레시 토큰 검증을 통해 구조 확인
        val extractedMemberId = jwtProvider.validateTokenAndGetMemberId(jwt.refreshToken)
        assertThat(extractedMemberId).isEqualTo(789L)
    }

    @Test
    fun `유효한 토큰으로 memberId 추출에 성공한다`() {
        // given
        val memberId = "100"
        val jwt = jwtProvider.generateTokens(memberId)

        // when
        val result = jwtProvider.validateTokenAndGetMemberId(jwt.refreshToken)

        // then
        assertThat(result).isEqualTo(100L)
    }

    @Test
    fun `null 토큰으로 검증시 예외가 발생한다`() {
        // when & then
        val exception = assertThrows<BaseException> {
            jwtProvider.validateTokenAndGetMemberId(null)
        }

        assertThat(exception.errorType).isEqualTo(ErrorType.UNAUTHORIZED)
        assertThat(exception.message).isEqualTo("인증에 실패했습니다.")
    }

    @Test
    fun `잘못된 형식의 토큰으로 검증시 예외가 발생한다`() {
        // when & then
        val exception = assertThrows<BaseException> {
            jwtProvider.validateTokenAndGetMemberId("invalid-token-format")
        }

        assertThat(exception.errorType).isEqualTo(ErrorType.UNAUTHORIZED)
        assertThat(exception.message).isEqualTo("인증에 실패했습니다.")
    }

    @Test
    fun `유효한 액세스 토큰으로 검증 및 memberId 추출에 성공한다`() {
        // given
        val memberId = "200"
        val jwt = jwtProvider.generateTokens(memberId)

        // when
        val result = jwtProvider.validateAccessTokenAndGetMemberId(jwt.accessToken)

        // then
        assertThat(result).isEqualTo(200L)
    }

    @Test
    fun `리프레시 토큰을 액세스 토큰 검증에 사용시 예외가 발생한다`() {
        // given
        val memberId = "300"
        val jwt = jwtProvider.generateTokens(memberId)

        // when & then
        val exception = assertThrows<BaseException> {
            jwtProvider.validateAccessTokenAndGetMemberId(jwt.refreshToken)
        }

        assertThat(exception.errorType).isEqualTo(ErrorType.UNAUTHORIZED)
        assertThat(exception.message).isEqualTo("잘못된 토큰입니다.")
    }

    @Test
    fun `null 토큰으로 액세스 토큰 검증시 예외가 발생한다`() {
        // when & then
        val exception = assertThrows<BaseException> {
            jwtProvider.validateAccessTokenAndGetMemberId(null)
        }

        assertThat(exception.errorType).isEqualTo(ErrorType.UNAUTHORIZED)
        assertThat(exception.message).isEqualTo("인증에 실패했습니다.")
    }

    @Test
    fun `잘못된 형식의 액세스 토큰으로 검증시 예외가 발생한다`() {
        // when & then
        val exception = assertThrows<BaseException> {
            jwtProvider.validateAccessTokenAndGetMemberId("invalid-access-token-format")
        }

        assertThat(exception.errorType).isEqualTo(ErrorType.UNAUTHORIZED)
        assertThat(exception.message).isEqualTo("인증에 실패했습니다.")
    }

    @Test
    fun `서로 다른 memberId로 생성된 토큰들이 올바르게 구분된다`() {
        // given
        val memberId1 = "1001"
        val memberId2 = "1002"

        // when
        val jwt1 = jwtProvider.generateTokens(memberId1)
        val jwt2 = jwtProvider.generateTokens(memberId2)

        // then
        val extractedMemberId1 = jwtProvider.validateTokenAndGetMemberId(jwt1.refreshToken)
        val extractedMemberId2 = jwtProvider.validateTokenAndGetMemberId(jwt2.refreshToken)

        assertThat(extractedMemberId1).isEqualTo(1001L)
        assertThat(extractedMemberId2).isEqualTo(1002L)
        assertThat(extractedMemberId1).isNotEqualTo(extractedMemberId2)
    }

    @Test
    fun `동일한 memberId로 생성된 토큰들이 같은 정보를 담고 있다`() {
        // given
        val memberId = "500"

        // when
        val jwt1 = jwtProvider.generateTokens(memberId)
        val jwt2 = jwtProvider.generateTokens(memberId)
        val jwt3 = jwtProvider.generateTokens(memberId)

        // then
        // 모든 토큰이 동일한 memberId를 가지는지 확인
        assertThat(jwtProvider.validateAccessTokenAndGetMemberId(jwt1.accessToken)).isEqualTo(500L)
        assertThat(jwtProvider.validateAccessTokenAndGetMemberId(jwt2.accessToken)).isEqualTo(500L)
        assertThat(jwtProvider.validateAccessTokenAndGetMemberId(jwt3.accessToken)).isEqualTo(500L)
        
        // 리프레시 토큰도 동일한 memberId를 가져야 함
        assertThat(jwtProvider.validateTokenAndGetMemberId(jwt1.refreshToken)).isEqualTo(500L)
        assertThat(jwtProvider.validateTokenAndGetMemberId(jwt2.refreshToken)).isEqualTo(500L)
        assertThat(jwtProvider.validateTokenAndGetMemberId(jwt3.refreshToken)).isEqualTo(500L)
    }


    @Test
    fun `액세스 토큰과 리프레시 토큰이 다른 타입으로 구분된다`() {
        // given
        val memberId = "700"
        val jwt = jwtProvider.generateTokens(memberId)

        // when & then
        // 액세스 토큰은 액세스 토큰 검증에 성공
        assertThat(jwtProvider.validateAccessTokenAndGetMemberId(jwt.accessToken)).isEqualTo(700L)

        // 리프레시 토큰은 일반 토큰 검증에 성공하지만 액세스 토큰 검증에 실패
        assertThat(jwtProvider.validateTokenAndGetMemberId(jwt.refreshToken)).isEqualTo(700L)

        assertThrows<BaseException> {
            jwtProvider.validateAccessTokenAndGetMemberId(jwt.refreshToken)
        }
    }
}