package nexters.tuk.contract

class BaseException(
    val errorType: ErrorType,
    customMessage: String? = null,
) : RuntimeException(customMessage ?: errorType.message)