package nexters.tuk.domain.invitation

import org.springframework.data.jpa.repository.JpaRepository

interface InvitationRepository : JpaRepository<Invitation, Long> {
    fun findByGatheringId(gatheringId: Long): List<Invitation>
}