package nexters.tuk.domain.push

import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

interface PushApiClient {
    @PostExchange("/api/v1/push/send")
    fun sendPushNotification(
        @RequestBody request: PushDto.Push,
    ): ApiResponse<PushDto.Push>
}