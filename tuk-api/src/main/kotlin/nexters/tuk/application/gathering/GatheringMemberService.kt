package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.response.GatheringMemberResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.gathering.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringMemberService(
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
) {
    @Transactional
    fun joinGathering(gatheringId: Long, memberId: Long): GatheringMemberResponse.JoinGathering {
        val gathering = gatheringRepository.findByIdOrThrow(gatheringId)
        if (gathering.hasMember(memberId)) {
            throw BaseException(ErrorType.BAD_REQUEST, "이미 가입된 사용자입니다.")
        }

        val gatheringMember = GatheringMember.registerMember(gathering, memberId)
            .let { gatheringMemberRepository.save(it) }

        return GatheringMemberResponse.JoinGathering(gatheringMember.id)
    }

    private fun Gathering.hasMember(memberId: Long): Boolean {
        return gatheringMemberRepository.findByGatheringAndMemberId(this, memberId) != null
    }

    @Transactional(readOnly = true)
    fun getMemberGatherings(memberId: Long): List<GatheringMemberResponse.MemberGatherings> {
        val gatheringMembers = gatheringMemberRepository.findAllByMemberId(memberId)

        return gatheringMembers
            .map { memberGathering ->
                GatheringMemberResponse.MemberGatherings(
                    memberGathering.gathering.id,
                    memberGathering.gathering.name,
                    memberGathering.gathering.intervalDays.toInt(),
                )
            }.sortedBy { it.name }
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