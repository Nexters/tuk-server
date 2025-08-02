package nexters.tuk.application.proposal.vo

data class ProposalPurpose(
    val where: String,
    val time: String,
    val what: String,
) {
    override fun toString(): String {
        return "$where\n$time\n$what"
    }
}