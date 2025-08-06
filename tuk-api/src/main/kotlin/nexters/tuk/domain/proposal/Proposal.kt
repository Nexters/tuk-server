package nexters.tuk.domain.proposal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import nexters.tuk.domain.BaseEntity
import org.hibernate.annotations.SQLRestriction

// FIXME: 모임 생성을 위한 임시 제안 엔티티
@SQLRestriction("deleted_at is NULL")
@Table(name = "proposal")
@Entity
class Proposal private constructor(
    @Column(name = "member_id", nullable = false)
    val proposerId: Long,

    @Column(name = "gathering_id", nullable = false)
    val gatheringId: Long,

    @Column(nullable = false)
    val purpose: String,
) : BaseEntity() {
    companion object {
        fun publish(gatheringId: Long, proposerId: Long, purpose: String): Proposal {
            return Proposal(
                proposerId = proposerId,
                gatheringId = gatheringId,
                purpose = purpose
            )
        }
    }
}