package nexters.tuk.application.gathering.dto.response

import io.swagger.v3.oas.annotations.media.Schema

class GatheringResponse {
    data class Generate(
        @Schema(description = "생성된 모임 id")
        val gatheringId: Long
    )

    data class GatheringOverviews(
        @Schema(description = "총 데이터 수")
        val size: Int,
        @Schema(description = "사용자 모임 데이터 리스트")
        val gatheringOverviews: List<GatheringOverview>,
    ) {
        data class GatheringOverview(
            @Schema(description = "모임명")
            val gatheringName: String,
            @Schema(description = "현재부터 마지막 모임까지 개월수")
            val monthsSinceLastGathering: Int
        )
    }
}