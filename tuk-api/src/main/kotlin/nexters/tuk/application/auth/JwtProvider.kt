package nexters.tuk.application.auth

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token.expires-in-days}") private val accessExpiresInDays: Long,
    @Value("\${jwt.refresh-token.expires-in-days}") private val refreshExpiresInDays: Long,
) {
    private enum class TokenType { ACCESS, REFRESH }

    private val secretKey: SecretKey by lazy {
        val keyBytes = Decoders.BASE64.decode(secret)
        Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateTokens(subject: String): Jwt {
        return Jwt(
            accessToken = createToken(subject, TokenType.ACCESS),
            refreshToken = createToken(subject, TokenType.REFRESH),
            refreshExpiresIn = computeExpiresInDays(TokenType.REFRESH)
        )
    }

    private fun createToken(subject: String, tokenType: TokenType): String {
        val now = Instant.now()
        val expirationDays = when (tokenType) {
            TokenType.ACCESS -> accessExpiresInDays
            TokenType.REFRESH -> refreshExpiresInDays
        }
        val expiration = now.plus(expirationDays, ChronoUnit.DAYS)

        return Jwts.builder()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun computeExpiresInDays(tokenType: TokenType): Long {
        return when (tokenType) {
            TokenType.ACCESS -> accessExpiresInDays
            TokenType.REFRESH -> refreshExpiresInDays
        }
    }
}

data class Jwt(
    val accessToken: String,
    val refreshToken: String,
    val refreshExpiresIn: Long,
)