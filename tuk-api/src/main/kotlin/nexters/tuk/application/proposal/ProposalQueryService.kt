package nexters.tuk.application.proposal

import nexters.tuk.application.gathering.vo.RelativeTime
import nexters.tuk.application.proposal.dto.request.ProposalQuery
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.domain.proposal.ProposalQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProposalQueryService(
    private val proposalQueryRepository: ProposalQueryRepository,
) {
    @Transactional(readOnly = true)
    fun getMemberProposals(query: ProposalQuery.MemberProposals): ProposalResponse.MemberProposals {
        val fetched = proposalQueryRepository.findMemberProposals(
            query.memberId,
            query.pageSize,
            query.pageNumber
        )

        val hasNext = fetched.size > query.pageSize
        val proposals = if (hasNext) fetched.dropLast(1) else fetched

        val proposalOverviews = proposals.map {
            ProposalResponse.MemberProposals.ProposalOverview(
                proposalId = it.id,
                gatheringName = it.gatheringName,
                purpose = it.purpose,
                relativeTime = RelativeTime.from(it.createdAt)
            )
        }

        val unreadProposalCount = proposalQueryRepository.countUnreadMemberProposal(query.memberId)

        return ProposalResponse.MemberProposals(
            hasNext = hasNext,
            size = query.pageSize,
            pageNumber = query.pageNumber,
            unreadProposalCount = unreadProposalCount,
            proposalOverviews = proposalOverviews
        )
    }
}