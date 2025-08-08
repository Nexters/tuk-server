package nexters.tuk.application.auth.dto.request

import nexters.tuk.contract.device.TukClientInfo

class AuthCommand {
    sealed class SocialLogin {
        abstract val deviceInfo: TukClientInfo

        data class Google(
            val idToken: String,
            override val deviceInfo: TukClientInfo
        ) : SocialLogin()

        data class Apple(
            val idToken: String,
            override val deviceInfo: TukClientInfo
        ) : SocialLogin()
    }

    data class Refresh(
        val refreshToken: String
    )

    data class Onboarding(
        val name: String
    )
}