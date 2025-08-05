package nexters.tuk.infrastructure.mysql.gathering

import nexters.tuk.domain.gathering.Gathering
import org.springframework.data.jpa.repository.JpaRepository

interface GatheringJpaRepository : JpaRepository<Gathering, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): Gathering?
}