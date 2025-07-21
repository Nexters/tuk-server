package nexters.tuk.application.meeting.dto.response

import io.swagger.v3.oas.annotations.media.Schema

class MeetingResponse {
    data class Generate(
        @Schema(description = "생성된 모임 id")
        val meetingId: Long
    )

    data class MeetingOverviews(
        @Schema(description = "다음 페이지 존재 여부")
        val hasNext: Boolean,
        @Schema(description = "현재 페이지")
        val currentPage: Int,
        @Schema(description = "페이지당 데이터 수")
        val size: Int,
        @Schema(description = "사용자 모임 데이터 리스트")
        val meetingOverviews: List<MeetingOverview>,
    ) {
        data class MeetingOverview(
            @Schema(description = "모임명")
            val meetingName: String,
            @Schema(description = "현재부터 마지막 모임까지 개월수")
            val monthsSinceLastMeeting: Int
        )
    }
}