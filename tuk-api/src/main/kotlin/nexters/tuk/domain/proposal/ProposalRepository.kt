package nexters.tuk.domain.proposal

import org.springframework.data.jpa.repository.JpaRepository

interface ProposalRepository : JpaRepository<Proposal, Long> {
    fun findByGatheringId(gatheringId: Long): List<Proposal>
}