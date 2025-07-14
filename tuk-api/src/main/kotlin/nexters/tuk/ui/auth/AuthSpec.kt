package nexters.tuk.ui.auth

import io.swagger.v3.oas.annotations.Operation
import nexters.tuk.application.auth.dto.response.AuthResponse
import nexters.tuk.contract.ApiResponse

interface AuthSpec {
    @Operation(summary = "구글 로그인")
    fun googleLogin(request: AuthDto.Request.GoogleLogin): ApiResponse<AuthResponse.Login>

    fun appleLogin(request: AuthDto.Request.AppleLogin) : ApiResponse<AuthResponse.Login>
}