package nexters.tuk.domain.proposal

import org.springframework.data.jpa.repository.JpaRepository

interface ProposalMemberRepository : JpaRepository<ProposalMember, Long>