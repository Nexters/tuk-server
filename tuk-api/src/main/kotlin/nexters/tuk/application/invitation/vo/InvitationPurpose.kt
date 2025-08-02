package nexters.tuk.application.invitation.vo

data class InvitationPurpose(
    val where: String,
    val time: String,
    val what: String,
) {
    override fun toString(): String {
        return "$where\n$time\n$what"
    }
}