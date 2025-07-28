package nexters.tuk.ui.push

import nexters.tuk.application.push.PushService
import nexters.tuk.application.push.dto.request.PushCommand
import nexters.tuk.application.push.dto.response.PushResponse
import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/push")
class PushController(
    private val pushService: PushService,
) : PushSpec {

    @PostMapping("/send")
    override fun sendPush(
        @RequestBody request: PushCommand.Push,
    ): ApiResponse<PushResponse.Push> {
        val result = pushService.sendPush(request)
        return ApiResponse.ok(result)
    }
}