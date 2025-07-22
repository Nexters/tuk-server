package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.application.member.MemberService
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
    private val memberService: MemberService,
) {
    @Transactional
    fun generateGathering(command: GatheringCommand.Generate): GatheringResponse.Generate {
        val member = memberService.findById(command.memberId)

        val gathering = Gathering.generate(member, command)
        val savedGathering = gatheringRepository.save(gathering)

        val gatheringMember = GatheringMember.registerHostMember(savedGathering, member)
        gatheringMemberRepository.save(gatheringMember)

        // TODO 알림 등록하기
        return GatheringResponse.Generate(
            gatheringId = savedGathering.id
        )
    }

    @Transactional(readOnly = true)
    fun getMemberGatherings(command: GatheringCommand.GetMemberGathering): GatheringResponse.GatheringOverviews {
        val member = memberService.findById(command.memberId)

        val gatheringMember = gatheringMemberRepository.findAllByMemberOrderByGatheringName(member)
        val overViews = gatheringMember
            .map { it.gathering }
            .map {
                GatheringResponse.GatheringOverviews.GatheringOverview(
                    it.name,
                    it.lastGatheringDate.monthsAgo()
                )
            }

        return GatheringResponse.GatheringOverviews(
            size = overViews.size,
            gatheringOverviews = overViews
        )
    }

    private fun LocalDate.monthsAgo(): Int {
        val daysBetween = ChronoUnit.DAYS.between(this, LocalDate.now())
        return (daysBetween / 30).toInt()
    }
}