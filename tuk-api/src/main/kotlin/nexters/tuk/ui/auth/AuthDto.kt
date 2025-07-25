package nexters.tuk.ui.auth

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.auth.dto.request.AuthCommand

class AuthDto {
    class Request {
        data class GoogleLogin(
            @Schema(description = "Google ID Token")
            val idToken: String,
            @Schema(description = "유저 디바이스 정보")
            val deviceInfo: DeviceInfo,
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
            val deviceInfo: DeviceInfo,
        ) {
            fun toCommand(): AuthCommand.SocialLogin.Apple {
                return AuthCommand.SocialLogin.Apple(
                    idToken = idToken,
                )
            }
        }

        // TODO: device 테이블 생성 필요
        data class DeviceInfo(
            @Schema(description = "디바이스 ID")
            val deviceId: String,
            @Schema(description = "디바이스 타입 - \"ios\", \"android\", \"web\" ")
            val deviceType: String,
            @Schema(description = "App Version")
            val appVersion: String? = null,
            @Schema(description = "OS Version")
            val osVersion: String? = null,
        )

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