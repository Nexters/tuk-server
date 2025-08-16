package nexters.tuk.application.push

enum class PushDeepLink(
    val link: String,
) {
    DEFAULT("tuk-app://tuk"),
    PROPOSAL("tuk-app://tuk/proposal-detail?proposalId=%s"),
    ;

    fun getLink(proposalId: Long): String {
        return this.link.format(proposalId)
    }
}