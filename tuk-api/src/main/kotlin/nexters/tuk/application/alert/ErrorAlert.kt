package nexters.tuk.application.alert

import java.time.ZonedDateTime

data class ErrorAlert(
    val statusCode: Int,
    val httpMethod: String,
    val path: String,
    val occurredAt: ZonedDateTime,
    val errorMessage: String
)