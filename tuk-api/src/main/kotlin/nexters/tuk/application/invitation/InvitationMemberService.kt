package nexters.tuk.application.invitation

import nexters.tuk.application.invitation.dto.response.InvitationMemberResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.invitation.InvitationMember
import nexters.tuk.domain.invitation.InvitationMemberRepository
import nexters.tuk.domain.invitation.InvitationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InvitationMemberService(
    private val invitationMemberRepository: InvitationMemberRepository,
    private val invitationRepository: InvitationRepository,
) {
    @Transactional
    fun publishGatheringMembers(
        invitationId: Long, memberIds: List<Long>
    ): InvitationMemberResponse.PublishedInvitationMembers {

        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { BaseException(ErrorType.NOT_FOUND, "찾을 수 없는 초대장입니다.") }

        val invitationMemberIds = memberIds.map {
            InvitationMember.publish(
                memberId = it,
                invitation = invitation,
            )
        }.let { invitationMemberRepository.saveAll(it) }.map { it.id }

        return InvitationMemberResponse.PublishedInvitationMembers(invitationMemberIds)
    }
}