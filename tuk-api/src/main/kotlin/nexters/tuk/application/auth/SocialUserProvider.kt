package nexters.tuk.application.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import nexters.tuk.application.auth.dto.AuthCommand
import nexters.tuk.application.auth.dto.SocialUserInfo
import nexters.tuk.application.member.SocialType
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

sealed interface SocialUserProvider {
    val socialType: SocialType
    fun getSocialUser(command: AuthCommand.SocialLogin): SocialUserInfo

    @Component
    class Google(
        @Value("\${oauth.google.client-id}") private val clientId: String,
    ) : SocialUserProvider {
        override val socialType = SocialType.GOOGLE
        private val googleIdTokenVerifier = GoogleIdTokenVerifier.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        )
            .setAudience(listOf(clientId))
            .build()

        override fun getSocialUser(command: AuthCommand.SocialLogin): SocialUserInfo {
            check(command is AuthCommand.SocialLogin.Google) { "$command is not supported For Google" }

            val idToken = verifyToken(command.idToken)
            val payload = idToken.payload

            return SocialUserInfo(
                socialType = SocialType.GOOGLE,
                socialId = payload.subject,
                email = payload.email
            )
        }

        private fun verifyToken(idTokenString: String): GoogleIdToken {
            return runCatching {
                googleIdTokenVerifier.verify(idTokenString)
            }.getOrElse {
                throw BaseException(ErrorType.UNAUTHORIZED, "구글 ID 토큰 검증에 실패했습니다.")
            }
        }
    }
}