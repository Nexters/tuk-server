package nexters.tuk.ui.auth

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.auth.dto.request.AuthCommand
import nexters.tuk.contract.device.TukClientInfo

class AuthDto {
    class Request {
        data class GoogleLogin(
            @Schema(description = "Google ID Token")
            val idToken: String,
            @Schema(description = "유저 디바이스 정보")
            val deviceInfo: TukClientInfo,
        ) {
            fun toCommand(): AuthCommand.SocialLogin.Google {
                return AuthCommand.SocialLogin.Google(
                    idToken = idToken,
                    deviceId = deviceInfo.deviceId,
                )
            }
        }

        data class AppleLogin(
            @Schema(description = "Apple ID Token")
            val idToken: String,
            @Schema(description = "유저 디바이스 정보")
            val deviceInfo: TukClientInfo,
        ) {
            fun toCommand(): AuthCommand.SocialLogin.Apple {
                return AuthCommand.SocialLogin.Apple(
                    idToken = idToken,
                )
            }
        }

        data class Refresh(
            @Schema(description = "Refresh token")
            val refreshToken: String,
        ) {
            fun toCommand(): AuthCommand.Refresh {
                return AuthCommand.Refresh(refreshToken)
            }
        }
    }
}