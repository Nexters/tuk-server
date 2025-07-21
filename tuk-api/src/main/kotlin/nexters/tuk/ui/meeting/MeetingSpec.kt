package nexters.tuk.ui.meeting

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import nexters.tuk.application.meeting.dto.response.MeetingResponse
import nexters.tuk.contract.ApiResponse


interface MeetingSpec {
    @Operation(
        summary = "모임 생성",
        security = [SecurityRequirement(name = "Authorization")]
    )
    fun generateMeeting(
        @Parameter(hidden = true) memberId: Long,
        request: MeetingDto.Request.Generate
    ): ApiResponse<MeetingResponse.Generate>
}