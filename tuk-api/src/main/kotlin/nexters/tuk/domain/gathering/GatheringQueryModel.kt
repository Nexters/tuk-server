package nexters.tuk.domain.gathering

class GatheringQueryModel {
    data class ProposalState(
        val sentCount: Int,
        val receivedCount: Int
    )
}