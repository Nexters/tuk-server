package nexters.tuk.application.auth.dto.request

class AuthCommand {
    sealed class SocialLogin {
        data class Google(
            val idToken: String,
            val deviceId: String,
        ) : SocialLogin()

        data class Apple(
            val idToken: String,
        ) : SocialLogin()
    }

    data class Refresh(
        val refreshToken: String
    )
}