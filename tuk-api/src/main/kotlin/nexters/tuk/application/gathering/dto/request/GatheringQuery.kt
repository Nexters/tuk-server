package nexters.tuk.application.gathering.dto.request

class GatheringQuery {
    data class MemberGathering(
        val memberId: Long,
    )

    data class GatheringDetail(
        val memberId: Long,
        val gatheringId: Long,
    )
}