package nexters.tuk.application.push.dto.response

class PushResponse {
    data class Push(
        val totalCount: Int,
        val successCount: Int,
        val failureCount: Int,
    )
}