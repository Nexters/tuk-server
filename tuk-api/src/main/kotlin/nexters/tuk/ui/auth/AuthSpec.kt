package nexters.tuk.ui.auth

import io.swagger.v3.oas.annotations.Operation
import nexters.tuk.application.auth.dto.response.AuthResponse
import nexters.tuk.contract.ApiResponse

interface AuthSpec {
    @Operation(summary = "구글 로그인")
    fun googleLogin(request: AuthDto.Request.GoogleLogin): ApiResponse<AuthResponse.Login>

    @Operation(summary = "애플 로그인")
    fun appleLogin(request: AuthDto.Request.AppleLogin): ApiResponse<AuthResponse.Login>

    @Operation(summary = "토큰 재발급")
    fun refreshAccessToken(request: AuthDto.Request.Refresh): ApiResponse<AuthResponse.Refresh>
}