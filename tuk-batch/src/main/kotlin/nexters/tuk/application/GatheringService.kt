package nexters.tuk.application

import nexters.tuk.domain.gathering.GatheringRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository
) {
    @Transactional
    fun updatePushStatus(gatheringId: Long) {
        gatheringRepository.findById(gatheringId)?.updatePushStatus()
    }
}