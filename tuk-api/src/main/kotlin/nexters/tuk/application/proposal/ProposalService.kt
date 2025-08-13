package nexters.tuk.application.proposal

import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.proposal.Proposal
import nexters.tuk.domain.proposal.ProposalRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProposalService(
    private val proposalRepository: ProposalRepository,
) {
    @Transactional(readOnly = true)
    fun getGatheringProposalStat(
        gatheringId: Long,
        memberId: Long
    ): ProposalResponse.ProposalStat {
        val proposals = proposalRepository.findByGatheringId(gatheringId)

        val sentCount = proposals.count { it.proposerId == memberId }
        val receivedCount = proposals.count { it.proposerId != memberId }

        return ProposalResponse.ProposalStat(sentCount, receivedCount)
    }

    @Transactional
    fun propose(command: ProposalCommand.Propose): ProposalResponse.Propose {
        val proposal = Proposal.publish(
            proposerId = command.memberId,
            purpose = command.purpose.toString(),
        ).let { proposalRepository.save(it) }

        return ProposalResponse.Propose(proposalId = proposal.id)
    }

    @Transactional
    fun addGathering(proposalId: Long, gatheringId: Long) {
        val proposal = proposalRepository.findById(proposalId).orElseThrow{
            BaseException(ErrorType.NOT_FOUND, "만남 초대장을 찾을 수 없습니다.")
        }

        proposal.registerGathering(gatheringId)
    }
}