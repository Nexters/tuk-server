package nexters.tuk.infrastructure.redis

import nexters.tuk.application.auth.Jwt
import nexters.tuk.domain.auth.JwtRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class JwtRedisRepository(
    private val redisTemplate: RedisTemplate<String, String>,
) : JwtRepository {
    companion object {
        private const val REFRESH_TOKEN_KEY_PREFIX = "refresh_token:member:"
    }

    override fun saveRefreshToken(memberId: Long, jwt: Jwt) {
        val valueOps = redisTemplate.opsForValue()
        val key = REFRESH_TOKEN_KEY_PREFIX + memberId
        valueOps.set(key, jwt.refreshToken, Duration.ofDays(jwt.refreshExpiresIn))
    }

    override fun findRefreshTokenById(memberId: Long): String? {
        val valueOps = redisTemplate.opsForValue()
        val key = REFRESH_TOKEN_KEY_PREFIX + memberId

        return valueOps[key]
    }
}