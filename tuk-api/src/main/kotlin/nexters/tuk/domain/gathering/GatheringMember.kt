package nexters.tuk.domain.gathering

import jakarta.persistence.*
import nexters.tuk.domain.BaseEntity
import nexters.tuk.domain.member.Member

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    val member: Member,

    @Column(name = "is_host", nullable = false, updatable = false)
    val isHost: Boolean = false,
) : BaseEntity() {
    companion object {
        fun registerHostMember(gathering: Gathering, member: Member): GatheringMember {
            return GatheringMember(gathering, member, true)
        }
    }
}