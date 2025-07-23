package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.request.GatheringQuery
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.application.invitation.InvitationService
import nexters.tuk.application.member.MemberService
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository,
    private val memberService: MemberService,
    private val gatheringMemberService: GatheringMemberService,
    private val invitationService: InvitationService,
) {
    @Transactional
    fun generateGathering(command: GatheringCommand.Generate): GatheringResponse.Generate {
        val hostMember = memberService.findById(command.memberId)
        val gathering = Gathering.generate(hostMember, command).also { gatheringRepository.save(it) }

        gatheringMemberService.initializeHost(gathering, hostMember)

        // TODO 알림 등록하기
        return GatheringResponse.Generate(
            gatheringId = gathering.id
        )
    }

    @Transactional(readOnly = true)
    fun getMemberGatherings(query: GatheringQuery.MemberGathering): GatheringResponse.GatheringOverviews {
        val member = memberService.findById(query.memberId)

        val gatherings = gatheringMemberService.getMemberGatherings(member)
        val overViews = gatherings
            .map {
                GatheringResponse.GatheringOverviews.GatheringOverview(
                    it.id, it.name, it.lastGatheringDate.monthsAgo()
                )
            }.sortedBy { it.gatheringName }

        return GatheringResponse.GatheringOverviews(
            size = overViews.size, gatheringOverviews = overViews
        )
    }

    private fun LocalDate.monthsAgo(): Int {
        return until(LocalDate.now(), ChronoUnit.MONTHS).toInt()
    }

    @Transactional(readOnly = true)
    fun getGatheringDetail(query: GatheringQuery.GatheringDetail): GatheringResponse.GatheringDetail {
        val gathering = gatheringRepository.findById(query.gatheringId)
            .orElseThrow { BaseException(ErrorType.NOT_FOUND, "모임을 찾을 수 없습니다.") }
        val member = memberService.findById(query.memberId)

        gatheringMemberService.verifyGatheringAccess(gathering, member)

        val gatheringInvitation = invitationService.getGatherInvitations(gathering)
        val sentInvitationCount = gatheringInvitation.count { it.host == member }
        val receivedInvitationCount = gatheringInvitation.size - sentInvitationCount

        return GatheringResponse.GatheringDetail(
            gatheringId = gathering.id,
            gatheringName = gathering.name,
            daysSinceFirstGathering = gathering.firstGatheringDate.daysAgo(),
            monthsSinceLastGathering = gathering.lastGatheringDate.monthsAgo(),
            sentInvitationCount = sentInvitationCount,
            receivedInvitationCount = receivedInvitationCount,
            members = gathering.buildMemberSummaries(),
        )
    }

    private fun LocalDate.daysAgo(): Int {
        return until(LocalDate.now(), ChronoUnit.DAYS).toInt()
    }

    private fun Gathering.buildMemberSummaries(): List<GatheringResponse.GatheringDetail.MemberSummary> {
        return gatheringMemberService.getGatheringMembers(this)
            .map { GatheringResponse.GatheringDetail.MemberSummary(it.id, it.name ?: "이름 없음") }
    }
}