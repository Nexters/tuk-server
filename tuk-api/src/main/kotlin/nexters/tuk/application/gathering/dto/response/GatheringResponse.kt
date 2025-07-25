package nexters.tuk.application.gathering.dto.response

class GatheringResponse {
    data class Generate(val gatheringId: Long)

    data class GatheringDetail(
        val id: Long,
        val name: String,
        val daysSinceFirstGathering: Int,
        val monthsSinceLastGathering: Int,
    )

    data class GatheringOverview(
        val id: Long,
        val name: String,
        val monthsSinceLastGathering: Int,
    )
}