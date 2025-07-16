package nexters.tuk.application.auth

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import nexters.tuk.application.auth.AppleIdTokenVerifier.Companion.EMAIL_CLAIM
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI


@Component
class AppleIdTokenVerifier(
    @Value("\${oauth.apple.client-id}") private val clientId: String,
    @Value("\${oauth.apple.jwk-url}") private val jwkUrl: String,
    @Value("\${oauth.apple.base-url}") private val baseUrl: String,
) {
    private val jwtProcessor = DefaultJWTProcessor<SecurityContext>().apply {

        val jwkSetUri = URI(jwkUrl)
        val jwkSource: JWKSource<SecurityContext> = JWKSourceBuilder
            .create<SecurityContext>(jwkSetUri.toURL())
            .retrying(true)
            .build()

        jwsKeySelector = JWSVerificationKeySelector(JWSAlgorithm.RS256, jwkSource)
        jwtClaimsSetVerifier = DefaultJWTClaimsVerifier(
            JWTClaimsSet.Builder()
                .issuer(baseUrl)
                .audience(clientId)
                .build(),
            setOf(SUBJECT_CLAIM, ISSUED_AT_CLAIM, EXPIRATION_TIME_CLAIM)
        )
    }

    fun verifyAndGetClaim(idToken: String): JWTClaimsSet {
        return runCatching {
            jwtProcessor.process(JWTParser.parse(idToken), null)
        }.getOrElse {
            throw BaseException(ErrorType.UNAUTHORIZED, "Apple ID 토큰 검증에 실패했습니다.")
        }
    }

    companion object {
        private const val SUBJECT_CLAIM = "sub"
        private const val ISSUED_AT_CLAIM = "iat"
        private const val EXPIRATION_TIME_CLAIM = "exp"
        const val EMAIL_CLAIM = "email"
    }
}

val JWTClaimsSet.email: String
    get() = this.getClaim(EMAIL_CLAIM) as String