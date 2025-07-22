package nexters.tuk.infrastructure

import com.google.firebase.messaging.*
import nexters.tuk.application.notification.NotificationMessage
import nexters.tuk.application.notification.NotificationSender
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FcmClient() : NotificationSender {
    private val logger = LoggerFactory.getLogger(FcmClient::class.java)

    override fun notifyMembers(
        tokens: List<String>,
        meesage: NotificationMessage,
        data: Map<String, String>?
    ) {
        val notification = Notification
            .builder()
            .setTitle(meesage.getTitle())
            .setBody(meesage.getBody())
            .build()

        val multicastMessage = MulticastMessage.builder()
            .addAllTokens(tokens)
            .setNotification(notification)
            .putAllData(data ?: emptyMap())
            .build()

        val response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage)

        if (response.failureCount > 0) {
            logFailedTokenResults(response, tokens)
        }
    }

    private fun logFailedTokenResults(
        response: BatchResponse,
        tokens: List<String>
    ) {
        response.responses.forEachIndexed { index, resp ->
            if (!resp.isSuccessful) {
                val token = tokens[index]
                val err = resp.exception
                val errorCode = err?.messagingErrorCode ?: MessagingErrorCode.UNAVAILABLE
                val errorMsg = err?.message ?: "no message"

                logger.warn("FCM 전송 실패 — token=$token, errorCode=$errorCode, message=$errorMsg")
            }
        }
    }
}