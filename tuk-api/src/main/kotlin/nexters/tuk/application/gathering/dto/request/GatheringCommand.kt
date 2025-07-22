package nexters.tuk.application.gathering.dto.request


class GatheringCommand {
    data class Generate(
        val memberId: Long,
        val gatheringName: String,
        val daysSinceLastGathering: Long,
        val gatheringIntervalDays: Long,
        val tags: List<String>,
    )
}