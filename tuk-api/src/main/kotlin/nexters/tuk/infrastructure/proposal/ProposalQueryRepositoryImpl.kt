package nexters.tuk.infrastructure.proposal

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import nexters.tuk.domain.gathering.QGathering
import nexters.tuk.domain.proposal.ProposalQueryModel
import nexters.tuk.domain.proposal.ProposalQueryRepository
import nexters.tuk.domain.proposal.QProposal
import nexters.tuk.domain.proposal.QProposalMember
import org.springframework.stereotype.Repository

@Repository
class ProposalQueryRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : ProposalQueryRepository {

    override fun findMemberProposals(
        memberId: Long,
        pageSize: Long,
        pageNumber: Long
    ): List<ProposalQueryModel.ProposalDetail> {
        val qGathering = QGathering.gathering
        val qProposal = QProposal.proposal
        val qProposalMember = QProposalMember.proposalMember

        val response = jpaQueryFactory
            .select(
                Projections.constructor(
                    ProposalQueryModel.ProposalDetail::class.java,
                    qProposal.id,
                    qGathering.name,
                    qProposal.purpose,
                    qProposalMember.isRead,
                    qProposal.createdAt,
                )
            )
            .distinct()
            .from(qProposalMember)
            .join(qProposalMember.proposal, qProposal)
            .join(qGathering).on(qProposal.gatheringId.eq(qGathering.id))
            .where(qProposalMember.memberId.eq(memberId))
            .orderBy(
                qProposalMember.isRead.asc(),
                qProposal.createdAt.desc()
            )
            .offset(pageSize * pageNumber)
            .limit(pageSize + 1L)
            .fetch()

        return response
    }

    override fun countUnreadMemberProposal(memberId: Long): Long {
        val qProposalMember = QProposalMember.proposalMember

        return jpaQueryFactory
            .select(qProposalMember.count())
            .from(qProposalMember)
            .where(qProposalMember.memberId.eq(memberId))
            .where(qProposalMember.isRead.isFalse)
            .where(qProposalMember.deletedAt.isNull)
            .fetchOne() ?: 0L
    }
}