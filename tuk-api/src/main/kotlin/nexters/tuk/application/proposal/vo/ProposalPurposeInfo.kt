package nexters.tuk.application.proposal.vo

data class ProposalPurposeInfo(
    val whereTag: String,
    val whenTag: String,
    val whatTag: String,
) {
    override fun toString(): String {
        return "$whereTag\n$whenTag\n$whatTag"
    }
}