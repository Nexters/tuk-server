package nexters.tuk.application.auth

import nexters.tuk.application.auth.dto.AuthCommand
import org.springframework.stereotype.Component

@Component
class SocialUserProviderFactory(
    private val googleProvider: SocialUserProvider.Google,
) {
    fun getProvider(command: AuthCommand.SocialLogin): SocialUserProvider {
        return when (command) {
            is AuthCommand.SocialLogin.Google -> googleProvider
        }
    }
}