package nexters.tuk.domain.invitation

import jakarta.persistence.*
import nexters.tuk.domain.BaseEntity
import nexters.tuk.domain.gathering.Gathering
import org.hibernate.annotations.SQLRestriction

// FIXME: 모임 생성을 위한 임시 초대장 엔티티
@SQLRestriction("deleted_at is NULL")
@Table(name = "invitation")
@Entity
class Invitation(
    @Column(name = "member_id", nullable = false)
    val inviterId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    val gathering: Gathering,

    @Column(nullable = false)
    val purpose: String,
) : BaseEntity()