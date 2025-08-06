package nexters.tuk.ui.purpose

import io.swagger.v3.oas.annotations.Operation
import nexters.tuk.application.purpose.dto.response.PurposeResponse
import nexters.tuk.contract.ApiResponse

interface PurposeSpec {
    @Operation(
        summary = "만남 목적 전체 조회",
    )
    fun getAllPurposes(): ApiResponse<PurposeResponse.Purposes>
}