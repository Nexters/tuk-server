package nexters.tuk.application.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

class AuthResponse {
    @Schema(name = "LoginResponse")
    data class Login(
        val memberId: Long,
        val accessToken: String,
        val refreshToken: String,
        val isFirstLogin: Boolean,
    )

    @Schema(name = "RefreshResponse")
    data class Refresh(
        val accessToken: String,
        val refreshToken: String,
    )
}