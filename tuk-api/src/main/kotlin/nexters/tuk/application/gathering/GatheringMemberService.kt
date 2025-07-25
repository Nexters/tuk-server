package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.gathering.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class GatheringMemberService(
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository
) {
    @Transactional
    fun registerMember(gatheringId: Long, memberId: Long): Long {
        val gathering = gatheringRepository.findByIdOrThrow(gatheringId)
        if (gathering.hasMember(memberId)) {
            throw BaseException(ErrorType.BAD_REQUEST, "이미 가입된 사용자입니다.")
        }

        val gatheringMember = GatheringMember.registerMember(gathering, memberId)
            .also { gatheringMemberRepository.save(it) }

        return gatheringMember.id
    }

    private fun Gathering.hasMember(memberId: Long): Boolean {
        return gatheringMemberRepository.findByGatheringAndMemberId(this, memberId) != null
    }

    @Transactional(readOnly = true)
    fun getMemberGatherings(memberId: Long): List<GatheringResponse.GatheringOverview> {
        val gatheringMembers = gatheringMemberRepository.findAllByMemberId(memberId)

        return gatheringMembers
            .map { it.gathering }
            .map {
                GatheringResponse.GatheringOverview(
                    it.id,
                    it.name,
                    it.lastGatheringDate.monthsAgo()
                )
            }.sortedBy { it.name }
    }

    private fun LocalDate.monthsAgo(): Int {
        return until(LocalDate.now(), ChronoUnit.MONTHS).toInt()
    }

    @Transactional(readOnly = true)
    fun verifyGatheringAccess(gatheringId: Long, memberId: Long) {
        val gathering = gatheringRepository.findByIdOrThrow(gatheringId)

        if (gathering.hasMember(memberId).not())
            throw BaseException(ErrorType.BAD_REQUEST, "사용자가 접근할 수 없는 모임입니다.")
    }

    @Transactional(readOnly = true)
    fun getGatheringMemberIds(gatheringId: Long): List<Long> {
        val gathering = gatheringRepository.findByIdOrThrow(gatheringId)
        val gatheringMembers = gatheringMemberRepository.findAllByGathering(gathering)

        return gatheringMembers.map { it.memberId }
    }
}