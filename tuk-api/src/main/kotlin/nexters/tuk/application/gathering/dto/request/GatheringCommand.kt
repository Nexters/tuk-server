package nexters.tuk.application.gathering.dto.request


class GatheringCommand {
    data class Generate(
        val memberId: Long,
        val gatheringName: String,
        val gatheringIntervalDays: Long,
        val tags: List<Long>,
    )

    data class Delete(
        val memberId: Long,
        val gatheringId: Long,
    )

    data class Update(
        val memberId: Long,
        val gatheringId: Long,
        val gatheringIntervalDays: Long,
    )
}