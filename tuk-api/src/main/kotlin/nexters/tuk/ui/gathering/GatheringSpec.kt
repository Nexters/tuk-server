package nexters.tuk.ui.gathering

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.gathering.dto.response.GatheringFacadeResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse


interface GatheringSpec {
    @Operation(
        summary = "모임 생성",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun generateGathering(
        @Parameter(hidden = true) memberId: Long,
        request: GatheringDto.Request.Generate
    ): ApiResponse<GatheringFacadeResponse.Generate>

    @Operation(
        summary = "사용자 모임 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun getMemberGathering(
        @Parameter(hidden = true) memberId: Long,
    ): ApiResponse<GatheringFacadeResponse.GatheringOverviews>

    @Operation(
        summary = "모임 상세 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun getGatheringDetail(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "모임 id") gatheringId: Long,
    ): ApiResponse<GatheringFacadeResponse.GatheringDetail>

    @Operation(
        summary = "모임 참여",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun joinGathering(
        @Parameter(hidden = true) memberId: Long,
        @Parameter(description = "모임 id") gatheringId: Long,
    ): ApiResponse<GatheringFacadeResponse.JoinGathering>
}