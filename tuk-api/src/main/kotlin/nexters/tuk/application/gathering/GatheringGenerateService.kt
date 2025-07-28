package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringGenerateService(
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberService: GatheringMemberService,
    private val gatheringTagService: GatheringTagService,
) {
    @Transactional
    fun generateGathering(command: GatheringCommand.Generate): GatheringResponse.Generate {
        val gathering = Gathering.generate(
            hostId = command.memberId,
            name = command.gatheringName,
            intervalDays = command.gatheringIntervalDays
        ).also { gatheringRepository.save(it) }

        val gatheringTags = gatheringTagService.addTags(gathering.id, command.tags)

        gatheringMemberService.joinGathering(
            gatheringId = gathering.id,
            memberId = command.memberId,
        )

        // TODO 알림 등록하기
        return GatheringResponse.Generate(
            gathering.id,
        )
    }
}