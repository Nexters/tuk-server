package nexters.tuk.domain.meeting

import jakarta.persistence.*
import nexters.tuk.domain.BaseEntity
import nexters.tuk.domain.member.Member

@Entity
@Table(
    name = "meeting_member",
    indexes = [
        Index(name = "idx_meeting_member", columnList = "meeting_id, member_id")
    ]
)
class MeetingMember private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false, updatable = false)
    val meeting: Meeting,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    val member: Member,

    @Column(name = "is_host", nullable = false, updatable = false)
    val isHost: Boolean = false,
) : BaseEntity() {
    companion object {
        fun registerHostMember(meeting: Meeting, member: Member): MeetingMember {
            return MeetingMember(meeting, member, true)
        }
    }
}