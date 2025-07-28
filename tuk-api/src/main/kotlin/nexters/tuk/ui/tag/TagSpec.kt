package nexters.tuk.ui.tag

import io.swagger.v3.oas.annotations.Operation
import nexters.tuk.application.tag.TagResponse
import nexters.tuk.contract.ApiResponse


interface TagSpec {
    @Operation(summary = "전체 태그 조회")
    fun getCategorizedTags(): ApiResponse<TagResponse.CategorizedTags>
}