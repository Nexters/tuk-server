package nexters.tuk.application.gathering.dto.request


class GatheringProposalCommand {
    data class AddProposal(
        val memberId: Long,
        val gatheringId: Long,
        val proposalId: Long,
    )
}