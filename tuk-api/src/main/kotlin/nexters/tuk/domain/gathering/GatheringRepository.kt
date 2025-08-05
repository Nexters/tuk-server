package nexters.tuk.domain.gathering

import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import org.springframework.data.jpa.repository.JpaRepository

interface GatheringRepository : JpaRepository<Gathering, Long>

fun GatheringRepository.findByIdOrThrow(gatheringId: Long): Gathering {
    return findById(gatheringId)
        .orElseThrow { BaseException(ErrorType.NOT_FOUND, "모임을 찾을 수 없습니다. [id=$gatheringId]") }
}