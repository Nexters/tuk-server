package nexters.tuk.application.proposal

import nexters.tuk.application.proposal.dto.response.ProposalResponse
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import nexters.tuk.domain.proposal.ProposalRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProposalService(
    private val proposalRepository: ProposalRepository,
    private val gatheringRepository: GatheringRepository,
) {
    @Transactional(readOnly = true)
    fun getGatheringProposalStat(gatheringId: Long, memberId: Long): ProposalResponse.ProposalStat {
        val gathering = gatheringRepository.findByIdOrThrow(gatheringId)
        val proposals = proposalRepository.findByGathering(gathering).toList()

        val sentCount = proposals.count { it.proposerId == memberId }
        val receivedCount = proposals.size - sentCount

        return ProposalResponse.ProposalStat(sentCount, receivedCount)
    }
}