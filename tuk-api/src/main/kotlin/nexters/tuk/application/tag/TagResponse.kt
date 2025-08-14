package nexters.tuk.application.tag


class TagResponse {
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