package nexters.tuk.domain.proposal

import jakarta.persistence.*
import nexters.tuk.domain.BaseEntity
import org.hibernate.annotations.SQLRestriction

@SQLRestriction("deleted_at is NULL")
@Table(name = "proposal_member")
@Entity
class ProposalMember private constructor(
    @Column(name = "member_id")
    val memberId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    val proposal: Proposal,

    val isRead: Boolean = false
) : BaseEntity() {
    companion object {
        fun publish(proposal: Proposal, memberId: Long): ProposalMember {
            return ProposalMember(
                memberId = memberId,
                proposal = proposal
            )
        }
    }
}