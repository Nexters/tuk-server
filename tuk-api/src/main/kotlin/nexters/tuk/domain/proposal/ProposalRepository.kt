package nexters.tuk.domain.proposal

import nexters.tuk.domain.gathering.Gathering
import org.springframework.data.jpa.repository.JpaRepository

interface ProposalRepository: JpaRepository<Proposal, Long> {
    fun findByGathering(gathering: Gathering): List<Proposal>
}