package nexters.tuk.ui.tag

import nexters.tuk.application.tag.TagResponse
import nexters.tuk.application.tag.TagService
import nexters.tuk.contract.ApiResponse
import org.springframework.stereotype.Controller

@Controller
class TagController(
    private val tagService: TagService
) : TagSpec {
    override fun getCategorizedTags(): ApiResponse<TagResponse.CategorizedTags> {
        val response = tagService.getCategorizedTags()

        return ApiResponse.ok(response)
    }
}