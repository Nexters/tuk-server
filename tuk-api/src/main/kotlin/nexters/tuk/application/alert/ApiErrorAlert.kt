package nexters.tuk.application.alert

import java.time.ZonedDateTime

data class ApiErrorAlert(
    val statusCode: Int,
    val httpMethod: String,
    val path: String,
    val occurredAt: ZonedDateTime,
    val errorMessage: String
)