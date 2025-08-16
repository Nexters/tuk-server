package nexters.tuk.infrastructure.querydsl.gathering

import com.querydsl.jpa.impl.JPAQueryFactory
import nexters.tuk.domain.gathering.GatheringQueryModel
import nexters.tuk.domain.gathering.GatheringQueryRepository
import nexters.tuk.domain.proposal.QProposal
import nexters.tuk.domain.proposal.QProposalMember
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
class GatheringQueryRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : GatheringQueryRepository {

    override fun findGatheringMemberProposalState(
        gatheringId: Long,
        memberId: Long
    ): GatheringQueryModel.ProposalState {
        val qProposal = QProposal.proposal
        val qProposalMember = QProposalMember.proposalMember

        val sentCount = jpaQueryFactory
            .select(qProposal.count())
            .from(qProposalMember)
            .join(qProposal).on(qProposal.id.eq(qProposalMember.proposal.id))
            .where(
                qProposal.gatheringId.eq(gatheringId),
                qProposalMember.memberId.eq(memberId),
                qProposal.proposerId.eq(memberId)
            )
            .fetchOne() ?: 0L

        val receivedCount = jpaQueryFactory
            .select(qProposal.count())
            .from(qProposalMember)
            .join(qProposal).on(qProposal.id.eq(qProposalMember.proposal.id))
            .where(
                qProposal.gatheringId.eq(gatheringId),
                qProposalMember.memberId.eq(memberId),
                qProposal.proposerId.ne(memberId)
            )
            .fetchOne() ?: 0L

        return GatheringQueryModel.ProposalState(
            sentCount = sentCount.toInt(),
            receivedCount = receivedCount.toInt()
        )
    }
}