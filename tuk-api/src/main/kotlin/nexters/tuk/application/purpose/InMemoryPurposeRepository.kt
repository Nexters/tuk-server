package nexters.tuk.application.purpose

import nexters.tuk.application.purpose.dto.response.PurposeResponse
import org.springframework.stereotype.Repository


@Repository
class InMemoryPurposeRepository {
    fun findAll(): PurposeResponse.Purposes {
        return PurposeResponse.Purposes(
            whatTags = InMemoryPurposeType.WHAT_TAG.tags,
            whereTags = InMemoryPurposeType.WHERE_TAG.tags,
            whenTags = InMemoryPurposeType.WHEN_TAG.tags
        )
    }
}
