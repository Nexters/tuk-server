package nexters.tuk.contract

data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }
    }
}