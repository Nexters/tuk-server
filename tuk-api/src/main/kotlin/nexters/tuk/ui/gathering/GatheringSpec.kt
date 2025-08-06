package nexters.tuk.ui.gathering

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.gathering.dto.response.GatheringMemberResponse
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.config.SwaggerConfig
import nexters.tuk.contract.ApiResponse


interface GatheringSpec {
    @Operation(
        summary = "모임 생성",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun generateGathering(
        memberId: Long,
        request: GatheringDto.Request.Generate,
    ): ApiResponse<GatheringResponse.Generate>

    @Operation(
        summary = "모임 수정",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun updateGathering(
        memberId: Long,
        gatheringId: Long,
        request: GatheringDto.Request.Update,
    ): ApiResponse<GatheringResponse.Simple>

    @Operation(
        summary = "사용자 모임 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun getMemberGathering(
        memberId: Long,
    ): ApiResponse<GatheringResponse.GatheringOverviews>

    @Operation(
        summary = "모임 상세 조회",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun getGatheringDetail(
        memberId: Long,
        @Parameter(description = "모임 id") gatheringId: Long,
    ): ApiResponse<GatheringResponse.GatheringDetail>

    @Operation(
        summary = "모임 참여",
        security = [SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)]
    )
    fun joinGathering(
        memberId: Long,
        @Parameter(description = "모임 id") gatheringId: Long,
    ): ApiResponse<GatheringMemberResponse.JoinGathering>

    @Operation(
        summary = "모임명 조회",
    )
    fun getGatheringName(
        gatheringId: Long
    ): ApiResponse<GatheringResponse.GatheringName>
}