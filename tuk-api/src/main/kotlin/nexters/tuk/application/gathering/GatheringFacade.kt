package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.request.GatheringQuery
import nexters.tuk.application.gathering.dto.response.GatheringFacadeResponse
import nexters.tuk.application.invitation.InvitationService
import nexters.tuk.application.member.MemberService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringFacade(
    private val gatheringService: GatheringService,
    private val gatheringMemberService: GatheringMemberService,
    private val invitationService: InvitationService,
    private val memberService: MemberService,
) {
    @Transactional
    fun generateGathering(command: GatheringCommand.Generate): GatheringFacadeResponse.Generate {
        val gatheringId = gatheringService.generateGathering(command)

        gatheringMemberService.registerMember(
            gatheringId = gatheringId,
            memberId = command.memberId,
        )

        // TODO 알림 등록하기
        return GatheringFacadeResponse.Generate(
            gatheringId,
        )
    }

    @Transactional(readOnly = true)
    fun getMemberGatherings(query: GatheringQuery.MemberGathering): GatheringFacadeResponse.GatheringOverviews {
        val gatherings = gatheringMemberService.getMemberGatherings(query.memberId)
        val gatheringOverviews = gatherings.map {
            GatheringFacadeResponse.GatheringOverviews.GatheringOverview(
                it.id, it.name, it.monthsSinceLastGathering
            )
        }

        return GatheringFacadeResponse.GatheringOverviews(gatherings.size, gatheringOverviews)
    }

    @Transactional(readOnly = true)
    fun getGatheringDetail(query: GatheringQuery.GatheringDetail): GatheringFacadeResponse.GatheringDetail {
        gatheringMemberService.verifyGatheringAccess(query.gatheringId, query.memberId)

        val invitationStat = invitationService.getGatheringInvitationStat(query.gatheringId, query.memberId)

        val gatheringMemberIds = gatheringMemberService.getGatheringMemberIds(query.gatheringId)
        val members = memberService.getMemberOverview(gatheringMemberIds).map {
                GatheringFacadeResponse.GatheringDetail.MemberOverview(it.memberId, it.memberName)
            }

        val gatheringDetail = gatheringService.getGatheringDetail(query.gatheringId)

        return GatheringFacadeResponse.GatheringDetail(
            gatheringId = gatheringDetail.id,
            gatheringName = gatheringDetail.name,
            daysSinceFirstGathering = gatheringDetail.daysSinceFirstGathering,
            monthsSinceLastGathering = gatheringDetail.monthsSinceLastGathering,
            sentInvitationCount = invitationStat.sentCount,
            receivedInvitationCount = invitationStat.receivedCount,
            members = members
        )
    }

    @Transactional
    fun joinGathering(command: GatheringCommand.JoinGathering): GatheringFacadeResponse.JoinGathering {
        gatheringMemberService.registerMember(
            gatheringId = command.gatheringId, memberId = command.memberId
        )

        return GatheringFacadeResponse.JoinGathering(command.gatheringId)
    }
}