package nexters.tuk.infrastructure

import com.google.firebase.messaging.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FcmClient {
    private val logger = LoggerFactory.getLogger(FcmClient::class.java)

    fun sendMulticast(tokens: List<String>, title: String, body: String, data: Map<String, String>? = null) {
        val notification = Notification
            .builder()
            .setTitle(title)
            .setBody(body)
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