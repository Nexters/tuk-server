package nexters.tuk.ui.auth

import nexters.tuk.application.auth.AuthService
import nexters.tuk.application.auth.dto.response.AuthResponse
import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) : AuthSpec {
    @PostMapping("/login/google")
    override fun googleLogin(
        @RequestBody request: AuthDto.Request.GoogleLogin,
    ): ApiResponse<AuthResponse.Login> {
        val response = authService.socialLogin(request.toCommand())

        return ApiResponse.ok(response)
    }

    @PostMapping("/login/apple")
    override fun appleLogin(
        @RequestBody request: AuthDto.Request.AppleLogin
    ): ApiResponse<AuthResponse.Login> {
        val response = authService.socialLogin(request.toCommand())

        return ApiResponse.ok(response)
    }

    @PostMapping("/refresh")
    override fun refreshAccessToken(
        @RequestBody request: AuthDto.Request.Refresh
    ): ApiResponse<AuthResponse.Refresh> {
        val response = authService.refreshAccessToken(request.toCommand())

        return ApiResponse.ok(response)
    }
}