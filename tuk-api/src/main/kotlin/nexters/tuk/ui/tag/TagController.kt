package nexters.tuk.ui.tag

import nexters.tuk.application.tag.TagResponse
import nexters.tuk.application.tag.TagService
import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/tags")
class TagController(
    private val tagService: TagService
) : TagSpec {

    @GetMapping
    override fun getCategorizedTags(): ApiResponse<TagResponse.CategorizedTags> {
        val response = tagService.getCategorizedTags()

        return ApiResponse.ok(response)
    }
}