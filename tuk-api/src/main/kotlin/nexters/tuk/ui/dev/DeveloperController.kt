package nexters.tuk.ui.dev

import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dev")
class DeveloperController {
    @GetMapping
    fun hello(): ApiResponse<String> {
        return ApiResponse.ok("Hello World!")
    }
}