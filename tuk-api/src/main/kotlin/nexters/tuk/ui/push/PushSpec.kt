package nexters.tuk.ui.push

import io.swagger.v3.oas.annotations.Operation
import nexters.tuk.application.push.dto.request.PushCommand
import nexters.tuk.application.push.dto.response.PushResponse
import nexters.tuk.contract.ApiResponse

interface PushSpec {
    @Operation(
        summary = "푸시 발송"
    )
    fun sendPush(
        request: PushCommand.Push,
    ): ApiResponse<PushResponse.Push>
}