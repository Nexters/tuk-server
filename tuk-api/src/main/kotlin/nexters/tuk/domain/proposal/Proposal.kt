package nexters.tuk.domain.proposal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import nexters.tuk.domain.BaseEntity
import org.hibernate.annotations.SQLRestriction

@SQLRestriction("deleted_at is NULL")
@Table(name = "proposal")
@Entity
class Proposal private constructor(
    @Column(name = "member_id", nullable = false, updatable = false)
    val proposerId: Long,

    @Column(name = "gathering_id", nullable = true)
    var gatheringId: Long? = null,

    @Column(nullable = false)
    val purpose: String,
) : BaseEntity() {
    companion object {
        fun publish(proposerId: Long, purpose: String): Proposal {
            return Proposal(
                proposerId = proposerId,
                purpose = purpose
            )
        }
    }

    fun registerGathering(gatheringId: Long) {
        require(this.gatheringId == null) { "이미 모임이 등록된 초대장입니다." }
        this.gatheringId = gatheringId
    }
}