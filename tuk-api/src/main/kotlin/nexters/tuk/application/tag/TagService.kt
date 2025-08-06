package nexters.tuk.application.tag

import nexters.tuk.config.CacheConfig
import nexters.tuk.domain.tag.TagRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class TagService(
    private val tagRepository: TagRepository
) {
    @Cacheable(
        cacheNames = ["tag:all"],
        cacheManager = CacheConfig.CACHE_WITH_30_DAYS
    )
    fun getCategorizedTags(): TagResponse.CategorizedTags {
        val tags = tagRepository.findAllWithCategory()

        val categoryGroups = tags.groupBy { it.category }
            .map { (category, categoryTags) ->
                val tagItems = categoryTags.map {
                    TagResponse.CategorizedTags.CategoryGroup.TagItem(
                        id = it.id,
                        name = it.name,
                    )
                }
                TagResponse.CategorizedTags.CategoryGroup(
                    category.name, tagItems
                )
            }

        return TagResponse.CategorizedTags(categoryGroups)
    }
}
