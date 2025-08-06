package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringCommandService(
    private val gatheringRepository: GatheringRepository,
) {
    @Transactional
    fun updateGathering(command: GatheringCommand.Update): GatheringResponse.Simple {
        val gathering = gatheringRepository.findByIdOrThrow(command.gatheringId)
        gathering.update(command)

        return GatheringResponse.Simple(
            gatheringId = gathering.id,
            gatheringName = gathering.name,
            intervalDays = gathering.intervalDays,
        )
    }
}