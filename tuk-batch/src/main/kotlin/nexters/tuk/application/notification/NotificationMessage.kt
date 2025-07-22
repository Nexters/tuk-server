package nexters.tuk.application.notification

interface NotificationMessage {
    fun getTitle(): String
    fun getBody(): String
}

class TukNotificationMessage(
    private val meetingId: Long,
    private val days: Long,
) : NotificationMessage {

    override fun getTitle(): String {
        TODO("Not yet implemented")
    }

    override fun getBody(): String {
        TODO("Not yet implemented")
    }
}

class InvitationNotificationMessage(
    private val meetingId: Long,
    private val purpose: String,
) : NotificationMessage {

    override fun getTitle(): String {
        TODO("Not yet implemented")
    }

    override fun getBody(): String {
        TODO("Not yet implemented")
    }
}