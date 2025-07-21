package nexters.tuk.application.meeting.dto.response

import io.swagger.v3.oas.annotations.media.Schema

class MeetingResponse {
    data class Generate(
        @Schema(description = "생성된 모임 id")
        val meetingId: Long
    )
}