package nexters.tuk.domain.proposal

import jakarta.persistence.*
import nexters.tuk.domain.BaseEntity
import nexters.tuk.domain.gathering.Gathering
import org.hibernate.annotations.SQLRestriction

// FIXME: 모임 생성을 위한 임시 제안 엔티티
@SQLRestriction("deleted_at is NULL")
@Table(name = "proposal")
@Entity
class Proposal(
    @Column(name = "member_id", nullable = false)
    val proposerId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    val gathering: Gathering,

    @Column(nullable = false)
    val purpose: String,
) : BaseEntity() {
    companion object {
        fun publish(gathering: Gathering, proposerId: Long, purpose: String): Proposal {
            return Proposal(
                proposerId = proposerId,
                gathering = gathering,
                purpose = purpose
            )
        }
    }
}