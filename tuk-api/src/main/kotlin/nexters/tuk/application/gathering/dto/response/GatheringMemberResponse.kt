package nexters.tuk.application.gathering.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

class GatheringMemberResponse {
    @Schema(name = "JoinGatheringResponse")
    data class JoinGathering(
        val id: Long,
    )

    data class MemberGatherings(
        val id: Long,
        val name: String,
        val lastPushedAt: LocalDateTime,
    )
}