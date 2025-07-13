package nexters.tuk.ui.auth

import nexters.tuk.application.auth.dto.AuthCommand

class AuthDto {
    class Request {
        data class GoogleLogin(
            val idToken: String,
            val deviceId: String,
        ) {
            fun toCommand(): AuthCommand.SocialLogin.Google {
                return AuthCommand.SocialLogin.Google(
                    idToken = idToken,
                    deviceId = deviceId,
                )
            }
        }
    }
}