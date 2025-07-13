package nexters.tuk.contract

data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val meta: Meta? = null,
) {
    data class Meta(
        val errorType: ErrorType,
        val errorMessage: String?,
    )

    companion object {
        fun <T> ok(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }

        fun fail(errorType: ErrorType, errorMessage: String): ApiResponse<Any?> {
            return ApiResponse(
                success = false,
                data = null,
                meta = Meta(
                    errorType = errorType,
                    errorMessage = errorMessage,
                )
            )
        }
    }
}