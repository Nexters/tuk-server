package nexters.tuk.application.auth.dto.response

class AuthResponse {
    data class Login(
        val memberId: Long,
        val accessToken: String,
        val refreshToken: String,
    )

    data class Refresh(
        val accessToken: String,
        val refreshToken: String,
    )
}