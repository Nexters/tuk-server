package nexters.tuk.domain.gathering

import org.springframework.data.jpa.repository.JpaRepository

interface GatheringTagRepository : JpaRepository<GatheringTag, Long> {
    fun findAllByGathering(gathering: Gathering): List<GatheringTag>
}