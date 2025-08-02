package nexters.tuk.domain.invitation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import nexters.tuk.domain.BaseEntity
import org.hibernate.annotations.SQLRestriction

// FIXME: 모임 생성을 위한 임시 초대장 엔티티
@SQLRestriction("deleted_at is NULL")
@Table(name = "invitation")
@Entity
class Invitation private constructor(
    @Column(name = "member_id", nullable = false)
    val inviterId: Long,

    @Column(name = "gathering_id", nullable = false)
    val gatheringId: Long,

    @Column(nullable = false)
    val purpose: String,
) : BaseEntity() {
    companion object {
        fun publish(gatheringId: Long, inviterId: Long, purpose: String): Invitation {
            return Invitation(
                inviterId = inviterId,
                gatheringId = gatheringId,
                purpose = purpose
            )
        }
    }
}