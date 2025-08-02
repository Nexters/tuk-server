package nexters.tuk.application.gathering.dto.request

import io.swagger.v3.oas.annotations.media.Schema


class GatheringCommand {
    data class Generate(
        val memberId: Long,
        val gatheringName: String,
        val gatheringIntervalDays: Long,
        val tags: List<Long>,
    )

    data class JoinGathering(
        val memberId: Long,
        val gatheringId: Long,
    )

    data class Update(
        val gatheringId: Long,
        val gatheringIntervalDays: Long,
    )
}