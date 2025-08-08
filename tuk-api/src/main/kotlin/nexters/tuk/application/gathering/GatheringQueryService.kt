package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringQuery
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.application.gathering.vo.RelativeTime
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.proposal.ProposalService
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringQueryService(
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberService: GatheringMemberService,
    private val proposalService: ProposalService,
    private val memberService: MemberService,
) {
    @Transactional(readOnly = true)
    fun getMemberGatherings(query: GatheringQuery.MemberGathering): GatheringResponse.GatheringOverviews {
        val gatheringOverviews = gatheringMemberService.getMemberGatherings(query.memberId).map {
            GatheringResponse.GatheringOverviews.GatheringOverview(
                gatheringId = it.id,
                gatheringName = it.name,
                lastNotificationRelativeTime = RelativeTime.fromDays(it.pushIntervalDays)
            )
        }

        return GatheringResponse.GatheringOverviews(gatheringOverviews.size, gatheringOverviews)
    }

    @Transactional(readOnly = true)
    fun getGatheringDetail(query: GatheringQuery.GatheringDetail): GatheringResponse.GatheringDetail {
        gatheringMemberService.verifyGatheringAccess(query.gatheringId, query.memberId)

        val proposalStat =
            proposalService.getGatheringProposalStat(query.gatheringId, query.memberId)

        val gatheringMemberIds = gatheringMemberService.getGatheringMemberIds(query.gatheringId)
        val members = memberService.getMemberOverviews(gatheringMemberIds).map {
            GatheringResponse.GatheringDetail.MemberOverview(it.memberId, it.memberName)
        }

        val gathering = gatheringRepository.findByIdOrThrow(query.gatheringId)

        return GatheringResponse.GatheringDetail(
            gatheringId = gathering.id,
            gatheringIntervalDays = gathering.intervalDays,
            gatheringName = gathering.name,
            lastPushRelativeTime = RelativeTime.from(gathering.lastPushedAt ?: gathering.createdAt),
            sentProposalCount = proposalStat.sentCount,
            receivedProposalCount = proposalStat.receivedCount,
            members = members
        )
    }

    @Transactional(readOnly = true)
    fun getGatheringName(gatheringId: Long): GatheringResponse.GatheringName {
        val gathering = gatheringRepository.findById(gatheringId).orElseThrow {
            BaseException(ErrorType.NOT_FOUND, "찾을 수 없는 모임입니다.")
        }

        return GatheringResponse.GatheringName(gathering.id, gathering.name)
    }
}