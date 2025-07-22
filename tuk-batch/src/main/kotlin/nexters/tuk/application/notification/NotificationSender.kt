package nexters.tuk.application.notification

interface NotificationSender {
    fun notifyMembers(
        tokens: List<String>,
        message: NotificationMessage,
        data: Map<String, String>? = null
    )
}