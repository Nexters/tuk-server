package nexters.tuk.application.purpose

import nexters.tuk.application.purpose.dto.response.PurposeResponse
import nexters.tuk.config.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PurposeService(
    private val inMemoryPurposeRepository: InMemoryPurposeRepository,
) {
    fun getAllPurposes(): PurposeResponse.Purposes {
        return inMemoryPurposeRepository.findAll()
    }
}