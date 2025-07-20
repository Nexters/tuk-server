package nexters.tuk.application.notification

interface MessageGenerator {
    fun getTitle(): String
    fun getBody(): String
}

class TukMessageGenerator(
    private val meetingId: Long,
    private val days: Long,
) : MessageGenerator {

    override fun getTitle(): String {
        TODO("Not yet implemented")
    }

    override fun getBody(): String {
        TODO("Not yet implemented")
    }
}

class InvitationMessageGenerator(
    private val meetingId: Long,
    private val purpose: String,
) : MessageGenerator {

    override fun getTitle(): String {
        TODO("Not yet implemented")
    }

    override fun getBody(): String {
        TODO("Not yet implemented")
    }
}