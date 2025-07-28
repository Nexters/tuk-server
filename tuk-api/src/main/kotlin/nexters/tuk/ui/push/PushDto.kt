package nexters.tuk.ui.push

import nexters.tuk.application.push.PushType

class PushDto {
    class Request {
        data class BulkPush(
            val recipients: List<PushRecipient>,
            val message: MessagePayload,
            val type: PushType,
            val metadata: Map<String, Any>? = null,
        )

        data class MessagePayload(
            val title: String,
            val body: String,
            val data: Map<String, Any>? = null,
        )
    }

    class Response {
        data class BulkPush(
            val totalCount: Int,
            val successCount: Int,
            val failureCount: Int,
            val results: List<Result>,
        )

        data class Result(
            val recipient: PushRecipient,
            val success: Boolean,
            val errorMessage: String? = null,
        )
    }

    data class PushRecipient(
        val userId: String? = null,
        val deviceToken: String? = null,
    )
}