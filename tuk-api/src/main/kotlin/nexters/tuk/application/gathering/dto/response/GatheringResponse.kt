package nexters.tuk.application.gathering.dto.response

import io.swagger.v3.oas.annotations.media.Schema

class GatheringResponse {
    @Schema(name = "GenerateResponse")
    data class Generate(
        @Schema(description = "생성된 모임 id")
        val gatheringId: Long
    )

    @Schema(name = "GatheringOverviewsResponse")
    data class GatheringOverviews(
        @Schema(description = "총 데이터 수")
        val totalCount: Int,
        @Schema(description = "사용자 모임 데이터 리스트")
        val gatheringOverviews: List<GatheringOverview>,
    ) {
        data class GatheringOverview(
            @Schema(description = "모임 id")
            val gatheringId: Long,
            @Schema(description = "모임명")
            val gatheringName: String,
            @Schema(description = "마지막 알림부터 현재까지 지난 개월수")
            val monthsSinceLastGathering: Int
        )
    }

    @Schema(name = "GatheringDetailResponse")
    data class GatheringDetail(
        @Schema(description = "모임 id")
        val gatheringId: Long,
        @Schema(description = "모임명")
        val gatheringName: String,
        @Schema(description = "마지막 알림부터 현재까지 지난 개월수")
        val monthsSinceLastNotification: Int,
        @Schema(description = "보낸 초대장 수")
        val sentInvitationCount: Int,
        @Schema(description = "받은 초대장 수")
        val receivedInvitationCount: Int,
        @Schema(description = "모임원")
        val members: List<MemberOverview>
    ) {
        data class MemberOverview(
            @Schema(description = "사용자 id")
            val memberId: Long,
            @Schema(description = "사용자 명")
            val memberName: String,
        )
    }
}