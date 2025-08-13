package nexters.tuk.contract

class SliceDto {
    data class SliceRequest(
        val pageNumber: Long = 1,
        val pageSize: Long = 10,
    )

    data class SliceResponse<T>(
        val hasNext: Boolean,
        val size: Long,
        val pageNumber: Long,
        val content: List<T>
    ) {
        companion object {
            fun <T> from(
                content: List<T>,
                page: SliceRequest
            ): SliceResponse<T> {
                val hasNext = content.size > page.pageSize
                val response = if (hasNext) content.dropLast(1) else content

                return SliceResponse(
                    hasNext = hasNext,
                    size = page.pageSize,
                    pageNumber = page.pageNumber,
                    content = response
                )
            }
        }
    }
}