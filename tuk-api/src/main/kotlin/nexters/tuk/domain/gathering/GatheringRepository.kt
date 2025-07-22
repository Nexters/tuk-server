package nexters.tuk.domain.gathering

import org.springframework.data.jpa.repository.JpaRepository

interface GatheringRepository: JpaRepository<Gathering, Long> {
}