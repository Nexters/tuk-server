package nexters.tuk.application.member.event

class MemberEvent {
    data class MemberDeleted(
        val memberId: Long,
    )
}