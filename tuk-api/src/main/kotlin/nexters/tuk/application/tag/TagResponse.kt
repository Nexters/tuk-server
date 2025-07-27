package nexters.tuk.application.tag

import io.swagger.v3.oas.annotations.media.Schema

class TagResponse {
    @Schema(name = "categorizedTags")
    data class CategorizedTags(
        val categories: List<CategoryGroup>
    ) {
        data class CategoryGroup(
            val categoryName: String,
            val tags: List<TagItem>
        ) {
            data class TagItem(
                val id: Long,
                val name: String
            )
        }
    }
}