package nexters.tuk.application.auth.dto

class AuthCommand {
    sealed class SocialLogin {
        data class Google(
            val idToken: String,
            val deviceId: String,
        ) : SocialLogin()
    }
}