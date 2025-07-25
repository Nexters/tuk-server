package nexters.tuk.domain.gathering

import jakarta.persistence.*
import nexters.tuk.domain.BaseEntity
import org.hibernate.annotations.SQLRestriction

@SQLRestriction("deleted_at is NULL")
@Entity
@Table(
    name = "gathering_member",
    indexes = [
        Index(name = "idx_gathering_member", columnList = "gathering_id, member_id")
    ]
)
class GatheringMember private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false, updatable = false)
    val gathering: Gathering,

    @Column(name = "member_id", nullable = false, updatable = false)
    val memberId: Long,

    @Column(name = "is_host", nullable = false)
    val isHost: Boolean,

    ) : BaseEntity() {
    companion object {
        fun registerMember(gathering: Gathering, memberId: Long): GatheringMember {
            return GatheringMember(gathering, memberId, gathering.isHost(memberId))
        }
    }
}