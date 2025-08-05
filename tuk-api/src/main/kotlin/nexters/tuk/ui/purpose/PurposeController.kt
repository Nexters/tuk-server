package nexters.tuk.ui.purpose

import nexters.tuk.application.purpose.PurposeService
import nexters.tuk.application.purpose.dto.response.PurposeResponse
import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/purposes")
class PurposeController(
    private val purposeService: PurposeService,
) {
    @GetMapping
    fun getAllPurposes(): ApiResponse<PurposeResponse.Purposes> {
        val response = purposeService.getAllPurposes()

        return ApiResponse.ok(response)
    }
}