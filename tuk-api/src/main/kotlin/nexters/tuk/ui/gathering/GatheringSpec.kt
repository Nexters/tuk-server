package nexters.tuk.ui.gathering

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.contract.ApiResponse


interface GatheringSpec {
    @Operation(
        summary = "모임 생성",
        security = [SecurityRequirement(name = "Authorization")]
    )
    fun generateGathering(
        @Parameter(hidden = true) memberId: Long,
        request: GatheringDto.Request.Generate
    ): ApiResponse<GatheringResponse.Generate>

    @Operation(
        summary = "사용자 모임 조회",
        security = [SecurityRequirement(name = "Authorization")]
    )
    fun getMemberGathering(
        @Parameter(hidden = true) memberId: Long,
    ): ApiResponse<GatheringResponse.GatheringOverviews>
}