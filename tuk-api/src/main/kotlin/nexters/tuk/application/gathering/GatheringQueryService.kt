package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringQuery
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.application.invitation.InvitationService
import nexters.tuk.application.member.MemberService
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
// TODO 알림 추가시 마지막 알림 날짜를 가져오는 로직 구현, 현재는 0으로 설정
class GatheringQueryService(
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberService: GatheringMemberService,
    private val invitationService: InvitationService,
    private val memberService: MemberService,
) {
    @Transactional(readOnly = true)
    fun getMemberGatherings(query: GatheringQuery.MemberGathering): GatheringResponse.GatheringOverviews {
        val gatheringOverviews = gatheringMemberService.getMemberGatherings(query.memberId).map {
            GatheringResponse.GatheringOverviews.GatheringOverview(
                gatheringId = it.id,
                gatheringName = it.name,
                relativeTime = 0.toRelativeTime()
            )
        }

        return GatheringResponse.GatheringOverviews(gatheringOverviews.size, gatheringOverviews)
    }

    private fun Int.toRelativeTime(): String {
        val daysInWeek = 7
        val daysInMonth = 30
        val daysInYear = 365

        return when {
            this == 0 -> "오늘"
            this < daysInWeek -> "${this}일 전"
            this < daysInMonth -> "${this / daysInWeek}주 전"
            this < daysInYear -> "${this / daysInMonth}개월 전"
            else -> "${this / daysInYear}년 전"
        }
    }

    @Transactional(readOnly = true)
    fun getGatheringDetail(query: GatheringQuery.GatheringDetail): GatheringResponse.GatheringDetail {
        gatheringMemberService.verifyGatheringAccess(query.gatheringId, query.memberId)

        val invitationStat = invitationService.getGatheringInvitationStat(query.gatheringId, query.memberId)

        val gatheringMemberIds = gatheringMemberService.getGatheringMemberIds(query.gatheringId)
        val members = memberService.getMemberOverview(gatheringMemberIds).map {
            GatheringResponse.GatheringDetail.MemberOverview(it.memberId, it.memberName)
        }

        val gatheringDetail = gatheringRepository.findByIdOrThrow(query.gatheringId)

        return GatheringResponse.GatheringDetail(
            gatheringId = gatheringDetail.id,
            gatheringName = gatheringDetail.name,
            monthsSinceLastNotification = 0,
            sentInvitationCount = invitationStat.sentCount,
            receivedInvitationCount = invitationStat.receivedCount,
            members = members
        )
    }
}