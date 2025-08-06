package nexters.tuk.application.purpose

import nexters.tuk.application.purpose.dto.response.PurposeResponse
import nexters.tuk.domain.purpose.PurposeRepository
import nexters.tuk.domain.purpose.PurposeType
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PurposeService(
    private val purposeRepository: PurposeRepository
) {
    @Cacheable("purposes:all")
    fun getAllPurposes(): PurposeResponse.Purposes {
        val purposes = purposeRepository.findAll().toList()
        val groupedPurposes = purposes.groupBy { it.type }

        return PurposeResponse.Purposes(
            whenTags = groupedPurposes[PurposeType.WHEN_TAG]?.map { it.tag } ?: emptyList(),
            whereTags = groupedPurposes[PurposeType.WHERE_TAG]?.map { it.tag } ?: emptyList(),
            whatTags = groupedPurposes[PurposeType.WHAT_TAG]?.map { it.tag } ?: emptyList(),
        )
    }
}