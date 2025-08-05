package nexters.tuk.application.gathering.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.gathering.vo.RelativeTime

class GatheringResponse {
    @Schema(name = "GenerateResponse")
    data class Generate(
        @Schema(description = "생성된 모임 id")
        val gatheringId: Long,
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
            @Schema(description = "상대 시간 타입 - \"오늘\", \"n일 전\", \"n주 전\", \"n개월 전\", \"n년 전\" ")
            val lastNotificationRelativeTime: RelativeTime,
        )
    }

    @Schema(name = "GatheringDetailResponse")
    data class GatheringDetail(
        @Schema(description = "모임 id")
        val gatheringId: Long,
        @Schema(description = "모임명")
        val gatheringName: String,
        @Schema(description = "상대 시간 타입 - \"오늘\", \"n일 전\", \"n주 전\", \"n개월 전\", \"n년 전\" ")
        val lastNotificationRelativeTime: RelativeTime,
        @Schema(description = "보낸 제안 수")
        val sentProposalCount: Int,
        @Schema(description = "받은 제안 수")
        val receivedProposalCount: Int,
        @Schema(description = "모임원")
        val members: List<MemberOverview>,
    ) {
        data class MemberOverview(
            @Schema(description = "사용자 id")
            val memberId: Long,
            @Schema(description = "사용자 명")
            val memberName: String,
        )
    }

    data class GatheringMembers(
        val gatheringId: Long,
        val memberIds: List<Long>,
    )
}