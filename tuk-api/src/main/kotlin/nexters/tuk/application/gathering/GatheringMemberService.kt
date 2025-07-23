package nexters.tuk.application.gathering

import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.member.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringMemberService(
    private val gatheringMemberRepository: GatheringMemberRepository,
) {
    @Transactional(readOnly = true)
    fun getGatheringMembers(gathering: Gathering): List<Member> {
        val gatheringMembers = gatheringMemberRepository.findAllByGathering(gathering)

        return gatheringMembers.map { it.member }
    }

    @Transactional(readOnly = true)
    fun getMemberGatherings(member: Member): List<Gathering> {
        val gatheringMembers = gatheringMemberRepository.findAllByMember(member)

        return gatheringMembers.map { it.gathering }
    }

    @Transactional
    fun initializeHost(gathering: Gathering, member: Member): GatheringMember {
        val gatheringMember = GatheringMember.registerHostMember(gathering, member)
        return gatheringMemberRepository.save(gatheringMember)
    }

    @Transactional(readOnly = true)
    fun verifyGatheringAccess(gathering: Gathering, member: Member) {
        gatheringMemberRepository.findByGatheringAndMember(gathering, member)
            ?: throw BaseException(ErrorType.BAD_REQUEST, "사용자가 접근할 수 없는 모임입니다.")
    }
}