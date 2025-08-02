package nexters.tuk.contract.device

import io.swagger.v3.oas.annotations.media.Schema

data class TukClientInfo(
    @Schema(description = "디바이스 ID")
    val deviceId: String,
    @Schema(description = "디바이스 타입 - \"ios\", \"android\", \"web\" ")
    val deviceType: String,
    @Schema(description = "App Version")
    val appVersion: String,
    @Schema(description = "OS Version")
    val osVersion: String,
    @Schema(description = "디바이스 토큰")
    val deviceToken: String?,
)