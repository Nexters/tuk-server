package nexters.tuk.domain.invitation

import nexters.tuk.domain.gathering.Gathering
import org.springframework.data.jpa.repository.JpaRepository

interface InvitationRepository: JpaRepository<Invitation, Long> {
    fun findByGathering(gathering: Gathering): List<Invitation>
}