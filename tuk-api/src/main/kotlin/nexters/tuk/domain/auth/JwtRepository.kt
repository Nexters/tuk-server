package nexters.tuk.domain.auth

import nexters.tuk.application.auth.Jwt

interface JwtRepository {
    fun saveRefreshToken(memberId: Long, jwt: Jwt)

    fun findRefreshTokenById(memberId: Long): String?
}