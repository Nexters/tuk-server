package nexters.tuk.application.alert

interface ErrorAlertSender {
    fun sendAlert(errorAlert: ErrorAlert)
}