package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.gathering.GatheringRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository,
) {
    @Transactional(readOnly = true)
    fun getGatheringName(gatheringId: Long): GatheringResponse.GatheringName {
        val gathering = gatheringRepository.findById(gatheringId).orElseThrow {
            BaseException(ErrorType.NOT_FOUND, "찾을 수 없는 모임입니다.")
        }

        return GatheringResponse.GatheringName(gathering.id, gathering.name)
    }
}