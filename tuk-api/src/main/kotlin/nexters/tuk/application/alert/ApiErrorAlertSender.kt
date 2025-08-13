package nexters.tuk.application.alert

interface ApiErrorAlertSender {
    fun sendError(apiErrorAlert: ApiErrorAlert)
}