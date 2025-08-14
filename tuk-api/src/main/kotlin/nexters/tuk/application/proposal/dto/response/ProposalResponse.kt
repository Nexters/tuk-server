package nexters.tuk.application.proposal.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.gathering.vo.RelativeTime

class ProposalResponse {
    data class ProposalStat(
        @Schema(description = "보낸 제안 수")
        val sentCount: Int,
        @Schema(description = "받은 제안 수")
        val receivedCount: Int,
    )

    data class Propose(
        val proposalId: Long,
    )

    data class ProposalOverview(
        val proposalId: Long,
        val gatheringName: String,
        val purpose: String,
        val relativeTime: RelativeTime,
    )

    data class ProposalDetail(
        val proposalId: Long,
        val gatheringId: Long?,
        val gatheringName: String?,
        val purpose: String,
        val relativeTime: RelativeTime,
    )
}