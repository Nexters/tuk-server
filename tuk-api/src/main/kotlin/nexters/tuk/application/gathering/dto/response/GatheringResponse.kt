package nexters.tuk.application.gathering.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.gathering.vo.RelativeTime

class GatheringResponse {
    data class Generate(
        @Schema(description = "생성된 모임 id")
        val gatheringId: Long,
    )

    data class Simple(
        val gatheringId: Long,
        val gatheringName: String,
        val intervalDays: Long,
    )

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
            val lastPushRelativeTime: RelativeTime,
        )
    }

    data class GatheringDetail(
        val gatheringId: Long,
        val gatheringIntervalDays: Long,
        val gatheringName: String,
        val lastPushRelativeTime: RelativeTime,
        val sentProposalCount: Int,
        val receivedProposalCount: Int,
        val members: List<MemberOverview>,
        val isHost: Boolean
    ) {
        data class MemberOverview(
            val memberId: Long,
            val memberName: String,
            val isMe: Boolean,
            val isHost: Boolean,
        )
    }

    data class GatheringName(
        val gatheringId: Long,
        val gatheringName: String
    )

    data class GatheringMembers(
        val gatheringId: Long,
        val memberIds: List<Long>,
    )
}