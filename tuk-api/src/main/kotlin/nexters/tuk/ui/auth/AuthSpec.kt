package nexters.tuk.ui.auth

import nexters.tuk.application.auth.dto.AuthCommand
import nexters.tuk.application.auth.dto.AuthResponse
import nexters.tuk.contract.ApiResponse

interface AuthSpec {
    fun googleLogin(request: AuthDto.Request.GoogleLogin): ApiResponse<AuthResponse.Login>
}