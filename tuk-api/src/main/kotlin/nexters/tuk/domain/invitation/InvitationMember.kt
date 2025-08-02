package nexters.tuk.domain.invitation

import jakarta.persistence.*
import nexters.tuk.domain.BaseEntity
import org.hibernate.annotations.SQLRestriction

@SQLRestriction("deleted_at is NULL")
@Table(name = "invitation_member")
@Entity
class InvitationMember private constructor(
    @Column(name = "member_id")
    val memberId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    val invitation: Invitation,

    val isRead: Boolean = false
) : BaseEntity() {
    companion object {
        fun publish(invitation: Invitation, memberId: Long): InvitationMember {
            return InvitationMember(
                memberId = memberId,
                invitation = invitation
            )
        }
    }
}