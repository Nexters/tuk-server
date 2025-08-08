package nexters.tuk.application.auth

import nexters.tuk.application.auth.dto.request.AuthCommand
import nexters.tuk.application.auth.dto.response.AuthResponse
import nexters.tuk.application.device.DeviceService
import nexters.tuk.application.device.dto.request.DeviceCommand
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.auth.JwtRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val socialUserProviderFactory: SocialUserProviderFactory,
    private val jwtProvider: JwtProvider,
    private val jwtRepository: JwtRepository,
    private val memberService: MemberService,
    private val deviceService: DeviceService,
) {
    @Transactional
    fun socialLogin(command: AuthCommand.SocialLogin): AuthResponse.Login {
        val userProvider = socialUserProviderFactory.getProvider(command)
        val userInfo = userProvider.getSocialUser(command)
        val member = memberService.login(
            MemberCommand.Login(
                email = userInfo.email,
                socialType = userInfo.socialType,
                socialId = userInfo.socialId,
            )
        )

        deviceService.updateDeviceToken(
            memberId = member.memberId,
            command = DeviceCommand.UpdateDeviceToken(
                command.deviceInfo
            )
        )

        val jwt = generateAndSaveTokens(member.memberId)

        return AuthResponse.Login(
            memberId = member.memberId,
            accessToken = jwt.accessToken,
            refreshToken = jwt.refreshToken,
            isFirstLogin = member.memberName.isNullOrBlank(),
        )
    }

    private fun generateAndSaveTokens(memberId: Long): Jwt {
        val jwt = jwtProvider.generateTokens(memberId.toString())
        jwtRepository.saveRefreshToken(memberId = memberId, jwt = jwt)
        return jwt
    }

    fun refreshAccessToken(command: AuthCommand.Refresh): AuthResponse.Refresh {
        val memberId = jwtProvider.validateTokenAndGetMemberId(command.refreshToken)
        val token = jwtRepository.findRefreshTokenById(memberId)
        if (token == null || token != command.refreshToken) {
            throw BaseException(ErrorType.BAD_REQUEST, "유효하지 않은 토큰입니다.")
        }

        val jwt = generateAndSaveTokens(memberId)

        return AuthResponse.Refresh(
            accessToken = jwt.accessToken,
            refreshToken = jwt.refreshToken,
        )
    }
}