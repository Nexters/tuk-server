package nexters.tuk.application.auth.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

class AuthResponse {
    @Schema(name = "LoginResponse")
    data class Login(
        val memberId: Long,
        val accessToken: String,
        val refreshToken: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "온보딩 필요시 입력해야 할 필드 목록")
        val requiredOnboardingData: List<String>
    )

    @Schema(name = "RefreshResponse")
    data class Refresh(
        val accessToken: String,
        val refreshToken: String,
    )
}