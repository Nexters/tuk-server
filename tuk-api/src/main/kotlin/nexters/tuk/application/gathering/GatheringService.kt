package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class GatheringService(
    private val gatheringRepository: GatheringRepository,
) {
    fun generateGathering(command: GatheringCommand.Generate): Long {
        val gathering = Gathering.generate(command).also { gatheringRepository.save(it) }

        return gathering.id
    }

    fun getGatheringDetail(gatheringId: Long): GatheringResponse.GatheringDetail {
        val gathering = gatheringRepository.findByIdOrThrow(gatheringId)

        return GatheringResponse.GatheringDetail(
            gathering.id,
            gathering.name,
            gathering.firstGatheringDate.daysAgo(),
            gathering.lastGatheringDate.monthsAgo(),
        )
    }

    private fun LocalDate.daysAgo(): Int {
        return until(LocalDate.now(), ChronoUnit.DAYS).toInt()
    }

    private fun LocalDate.monthsAgo(): Int {
        return until(LocalDate.now(), ChronoUnit.MONTHS).toInt()
    }
}