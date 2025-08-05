package nexters.tuk.application.proposal

import nexters.tuk.application.gathering.vo.RelativeTime
import nexters.tuk.application.proposal.dto.request.ProposalQuery
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.domain.proposal.ProposalQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

enum class ProposalDirection {
    SENT,
    RECEIVED,
}

@Service
class ProposalQueryService(
    private val proposalQueryRepository: ProposalQueryRepository,
) {
    @Transactional(readOnly = true)
    fun getMemberProposals(query: ProposalQuery.MemberProposals): ProposalResponse.MemberProposals {
        val memberProposals = proposalQueryRepository.findMemberProposals(
            memberId = query.memberId,
            pageSize = query.pageSize,
            pageNumber = query.pageNumber
        )

        val hasNext = memberProposals.size > query.pageSize
        val proposals = if (hasNext) memberProposals.dropLast(1) else memberProposals

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

    @Transactional(readOnly = true)
    fun getGatheringProposals(query: ProposalQuery.GatheringProposals): ProposalResponse.GatheringProposals {
        val gatheringProposals = proposalQueryRepository.findGatheringProposals(
            query.memberId,
            query.gatheringId,
            query.type,
            query.pageSize,
            query.pageNumber
        )

        val hasNext = gatheringProposals.size > query.pageSize
        val proposals = if (hasNext) gatheringProposals.dropLast(1) else gatheringProposals

        val proposalOverviews = proposals.map {
            ProposalResponse.MemberProposals.ProposalOverview(
                proposalId = it.id,
                gatheringName = it.gatheringName,
                purpose = it.purpose,
                relativeTime = RelativeTime.from(it.createdAt)
            )
        }

        return ProposalResponse.GatheringProposals(
            hasNext = hasNext,
            size = query.pageSize,
            pageNumber = query.pageNumber,
            proposalOverviews = proposalOverviews
        )
    }
}