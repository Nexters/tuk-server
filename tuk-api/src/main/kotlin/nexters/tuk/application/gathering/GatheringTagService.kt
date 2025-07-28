package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.response.GatheringTagResponse
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.GatheringTag
import nexters.tuk.domain.gathering.GatheringTagRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringTagService(
    private val gatheringRepository: GatheringRepository,
    private val gatheringTagRepository: GatheringTagRepository
) {
    @Transactional
    fun addTags(gatheringId: Long, tagsId: List<Long>): GatheringTagResponse.AddTag {
        val gathering = gatheringRepository.findByIdOrThrow(gatheringId)

        val gatheringTags = tagsId
            .map { GatheringTag.addTag(gathering, it) }
            .let { gatheringTagRepository.saveAll(it) }
            .toList()

        return GatheringTagResponse.AddTag(gatheringTags.map { it.id })
    }
}