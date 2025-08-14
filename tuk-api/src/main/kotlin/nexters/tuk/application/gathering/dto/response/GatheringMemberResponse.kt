package nexters.tuk.application.gathering.dto.response

import java.time.LocalDateTime

class GatheringMemberResponse {
    data class JoinGathering(
        val id: Long,
    )

    data class MemberGatherings(
        val id: Long,
        val name: String,
        val lastPushedAt: LocalDateTime,
    )
}