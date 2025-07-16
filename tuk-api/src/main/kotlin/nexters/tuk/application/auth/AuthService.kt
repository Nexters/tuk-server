package nexters.tuk.application.auth

import nexters.tuk.application.auth.dto.request.AuthCommand
import nexters.tuk.application.auth.dto.response.AuthResponse
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.domain.auth.JwtRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val socialUserProviderFactory: SocialUserProviderFactory,
    private val jwtProvider: JwtProvider,
    private val jwtRepository: JwtRepository,
    private val memberService: MemberService,
) {
    @Transactional
    fun socialLogin(command: AuthCommand.SocialLogin): AuthResponse.Login {
        val userProvider = socialUserProviderFactory.getProvider(command)
        val userInfo = userProvider.getSocialUser(command)
        val member = memberService.signUp(
            MemberCommand.SignUp(
                email = userInfo.email,
                socialType = userInfo.socialType,
                socialId = userInfo.socialId
            )
        )

        val jwt = jwtProvider.generateTokens(member.memberId.toString())
        jwtRepository.saveRefreshToken(memberId = member.memberId, jwt = jwt)

        return AuthResponse.Login(
            memberId = member.memberId,
            accessToken = jwt.accessToken,
            refreshToken = jwt.refreshToken,
        )
    }
}