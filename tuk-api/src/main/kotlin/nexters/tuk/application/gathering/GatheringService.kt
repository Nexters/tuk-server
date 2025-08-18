package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringService(
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
        ).let { gatheringRepository.save(it) }

        val gatheringTags = gatheringTagService.addTags(gathering.id, command.tags)

        gatheringMemberService.joinGathering(
            gatheringId = gathering.id,
            memberId = command.memberId,
        )

        return GatheringResponse.Generate(
            gathering.id,
        )
    }

    @Transactional
    fun updateGathering(command: GatheringCommand.Update): GatheringResponse.Simple {
        val gathering = gatheringRepository.findByIdOrThrow(command.gatheringId)

        validateHostPermission(gathering = gathering, memberId = command.memberId)

        gathering.update(command)

        return GatheringResponse.Simple(
            gatheringId = gathering.id,
            gatheringName = gathering.name,
            intervalDays = gathering.intervalDays,
        )
    }

    private fun validateHostPermission(
        gathering: Gathering,
        memberId: Long,
    ) {
        if (!gathering.isHost(memberId)) {
            throw BaseException(ErrorType.BAD_REQUEST, "수정 권한이 없습니다.")
        }
    }

    @Transactional
    fun deleteGathering(command: GatheringCommand.Delete) {
        val gathering = gatheringRepository.findByIdOrThrow(command.gatheringId)

        validateHostPermission(gathering = gathering, command.memberId)

        gathering.delete()
    }

    @Transactional
    fun updatePushStatus(gatheringId: Long) {
        gatheringRepository.findByIdOrThrow(gatheringId).updatePushStatus()
    }
}