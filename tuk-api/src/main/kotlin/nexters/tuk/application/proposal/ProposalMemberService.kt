package nexters.tuk.application.proposal

import nexters.tuk.application.proposal.dto.response.ProposalMemberResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.proposal.ProposalMember
import nexters.tuk.domain.proposal.ProposalMemberRepository
import nexters.tuk.domain.proposal.ProposalRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProposalMemberService(
    private val proposalMemberRepository: ProposalMemberRepository,
    private val proposalRepository: ProposalRepository,
) {
    @Transactional
    fun publishGatheringMembers(
        proposalId: Long, memberIds: List<Long>
    ): ProposalMemberResponse.PublishedProposalMembers {

        val proposal = proposalRepository.findById(proposalId)
            .orElseThrow { BaseException(ErrorType.NOT_FOUND, "찾을 수 없는 제안입니다.") }

        val proposalMemberIds = memberIds.map {
            ProposalMember.publish(
                memberId = it,
                proposal = proposal,
            )
        }.let { proposalMemberRepository.saveAll(it) }.map { it.id }

        return ProposalMemberResponse.PublishedProposalMembers(proposalMemberIds)
    }
}