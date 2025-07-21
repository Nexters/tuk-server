package nexters.tuk.ui.meeting

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
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

    @Operation(
        summary = "사용자 모임 조회",
        security = [SecurityRequirement(name = "Authorization")]
    )
    fun getMemberMeetings(
        @Parameter(hidden = true) memberId: Long,
        @Schema(description = "현재 페이지", defaultValue = "0") page: Int,
        @Schema(description = "페이지당 데이터 수", defaultValue = "10") size: Int
    ): ApiResponse<MeetingResponse.MeetingOverviews>
}