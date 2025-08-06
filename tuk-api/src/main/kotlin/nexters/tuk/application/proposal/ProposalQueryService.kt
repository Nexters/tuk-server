package nexters.tuk.application.proposal

import nexters.tuk.application.gathering.vo.RelativeTime
import nexters.tuk.application.proposal.dto.request.ProposalQuery
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.contract.SliceDto.SliceResponse
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
    fun getMemberProposals(query: ProposalQuery.MemberProposals): SliceResponse<ProposalResponse.ProposalOverview> {
        val memberProposals = proposalQueryRepository.findMemberProposals(
            memberId = query.memberId,
            page = query.page
        )

        val proposalOverviews = memberProposals.map {
            ProposalResponse.ProposalOverview(
                proposalId = it.id,
                gatheringName = it.gatheringName,
                purpose = it.purpose,
                relativeTime = RelativeTime.from(it.createdAt)
            )
        }

        return SliceResponse.from(proposalOverviews, query.page)
    }

    @Transactional(readOnly = true)
    fun getGatheringProposals(query: ProposalQuery.GatheringProposals): SliceResponse<ProposalResponse.ProposalOverview> {
        val gatheringProposals = proposalQueryRepository.findGatheringProposals(
            query.memberId,
            query.gatheringId,
            query.type,
            query.page
        )

        val proposalOverviews = gatheringProposals.map {
            ProposalResponse.ProposalOverview(
                proposalId = it.id,
                gatheringName = it.gatheringName,
                purpose = it.purpose,
                relativeTime = RelativeTime.from(it.createdAt)
            )
        }

        return SliceResponse.from(proposalOverviews, query.page)
    }

    @Transactional(readOnly = true)
    fun getProposal(proposalId: Long): ProposalResponse.ProposalDetail {
        val proposal = proposalQueryRepository.findProposalById(proposalId) ?: throw BaseException(
            ErrorType.NOT_FOUND, "존재하지 않는 만남 초대장입니다."
        )

        return ProposalResponse.ProposalDetail(
            proposalId = proposal.id,
            gatheringId = proposal.gatheringId,
            gatheringName = proposal.gatheringName,
            purpose = proposal.purpose,
            relativeTime = RelativeTime.from(proposal.createdAt)
        )
    }
}