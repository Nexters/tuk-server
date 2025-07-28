package nexters.tuk.infrastructure.fcm

import com.google.firebase.messaging.*
import nexters.tuk.application.push.PushSender
import nexters.tuk.application.push.dto.request.PushCommand
import nexters.tuk.application.push.dto.response.PushResponse
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

private data class ChunkResult(
    val successCount: Int,
    val failedTokens: List<String>,
)

@Component
class FcmPushSender : PushSender {
    private val logger = LoggerFactory.getLogger(FcmPushSender::class.java)

    companion object {
        private const val PUSH_CHUNK_SIZE = 100
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MILLIS = 1000L
    }

    @Async
    override fun send(
        recipients: List<PushCommand.PushRecipient>,
        message: PushCommand.MessagePayload,
    ) {
        when (recipients.size) {
            0 -> throw BaseException(ErrorType.BAD_REQUEST, "No valid device tokens found")

            1 -> singlePush(
                recipient = recipients.first(),
                message = message
            )

            else -> multicastPush(
                recipients = recipients,
                message = message,
            )
        }
    }

    private fun singlePush(
        recipient: PushCommand.PushRecipient,
        message: PushCommand.MessagePayload,
    ): PushResponse.Push {
        return sendWithRetry(listOf(recipient.deviceToken), message)
    }

    private fun multicastPush(
        recipients: List<PushCommand.PushRecipient>,
        message: PushCommand.MessagePayload,
    ): PushResponse.Push {
        val deviceTokens = extractValidTokens(recipients)
        return sendWithRetry(deviceTokens, message)
    }

    private fun sendWithRetry(
        deviceTokens: List<String>,
        message: PushCommand.MessagePayload,
    ): PushResponse.Push {
        var remainingTokens = deviceTokens.toMutableList()

        val totalCount = deviceTokens.size
        var totalSuccessCount = 0
        var totalFailureCount = 0

        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            if (remainingTokens.isEmpty()) {
                logger.info("All tokens processed successfully")
                return@repeat
            }

            val chunks = remainingTokens.chunked(PUSH_CHUNK_SIZE)
            val currentAttemptFailedTokens = mutableListOf<String>()

            logger.info("Retry attempt ${attempt + 1}/${MAX_RETRY_ATTEMPTS}, processing ${remainingTokens.size} tokens")

            chunks.forEach { chunk ->
                val result = if (chunk.size == 1) {
                    sendSingleToken(chunk.first(), message)
                } else {
                    sendMultipleTokens(chunk, message)
                }

                totalSuccessCount += result.successCount
                currentAttemptFailedTokens.addAll(result.failedTokens)
            }

            // 이번 시도에서 실패한 토큰들을 다음 재시도 대상으로 설정
            remainingTokens = currentAttemptFailedTokens

            // 마지막 시도가 아니고 실패한 토큰이 있으면 잠시 대기
            if (remainingTokens.isNotEmpty() && attempt < MAX_RETRY_ATTEMPTS - 1) {
                logger.info("Waiting ${RETRY_DELAY_MILLIS}ms before retry...")
                Thread.sleep(RETRY_DELAY_MILLIS)
            }
        }

        totalFailureCount = remainingTokens.size

        logger.info("Final result - Total: $totalCount, Success: $totalSuccessCount, Failure: $totalFailureCount")

        return PushResponse.Push(
            totalCount = totalCount,
            successCount = totalSuccessCount,
            failureCount = totalFailureCount,
        )
    }

    private fun sendSingleToken(
        token: String,
        message: PushCommand.MessagePayload,
    ): ChunkResult {
        return try {
            val pushMessage = buildSingleMessage(token, message)
            val messageId = FirebaseMessaging.getInstance().send(pushMessage)

            logger.debug("Single FCM message sent successfully. MessageId: $messageId")

            ChunkResult(
                successCount = 1,
                failedTokens = emptyList()
            )
        } catch (e: FirebaseMessagingException) {
            logger.debug("Failed to send single FCM message to token: $token, error: ${e.message}")

            // 만료된 토큰 처리
            if (e.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
                logger.info("unregistered token: $token")
            }

            val failedTokens = if (isRetryableError(e)) listOf(token) else emptyList()

            ChunkResult(
                successCount = 0,
                failedTokens = failedTokens
            )
        }
    }

    private fun sendMultipleTokens(
        chunk: List<String>,
        message: PushCommand.MessagePayload,
    ): ChunkResult {
        return try {
            val pushMessage = buildMulticastMessage(
                deviceTokens = chunk,
                message = message,
            )

            val response = FirebaseMessaging.getInstance().sendEachForMulticast(pushMessage)
            logger.debug("Multicast sent - Success: ${response.successCount}, Failure: ${response.failureCount}")

            val failedTokens = extractFailedTokens(response, chunk)

            // 만료된 토큰 로깅
            response.responses.forEachIndexed { index, sendResponse ->
                if (!sendResponse.isSuccessful) {
                    val error = sendResponse.exception
                    val token = failedTokens[index]

                    if (error?.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
                        logger.info("unregistered token: $token")
                    }
                }
            }

            ChunkResult(
                successCount = response.successCount,
                failedTokens = failedTokens
            )
        } catch (e: FirebaseMessagingException) {
            logger.error("Failed to send multicast chunk", e)
            ChunkResult(
                successCount = 0,
                failedTokens = chunk
            )
        }
    }

    private fun extractFailedTokens(
        response: BatchResponse,
        tokens: List<String>,
    ): List<String> {
        val failedTokens = mutableListOf<String>()

        response.responses.forEachIndexed { index, sendResponse ->
            if (!sendResponse.isSuccessful) {
                val error = sendResponse.exception

                // 일시적 오류만 재시도 대상으로 포함
                if (isRetryableError(error)) {
                    failedTokens.add(tokens[index])
                }

                logger.error(
                    "Token failed: ${tokens[index]}, error: ${error?.message}, retryable: ${
                        isRetryableError(
                            error
                        )
                    }"
                )
            }
        }

        return failedTokens
    }

    private fun isRetryableError(error: FirebaseMessagingException): Boolean {
        return when (error.messagingErrorCode) {
            MessagingErrorCode.INTERNAL -> true
            MessagingErrorCode.UNAVAILABLE -> true
            MessagingErrorCode.QUOTA_EXCEEDED -> true
            MessagingErrorCode.UNREGISTERED -> false // 만료된 토큰은 재시도하지 않음
            MessagingErrorCode.INVALID_ARGUMENT -> false // 잘못된 요청은 재시도하지 않음
            else -> false
        }
    }

    private fun extractValidTokens(recipients: List<PushCommand.PushRecipient>): List<String> {
        return recipients
            .map { it.deviceToken }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun buildSingleMessage(
        deviceToken: String,
        message: PushCommand.MessagePayload,
    ): Message {
        return Message.builder()
            .setToken(deviceToken)
            .setNotification(
                Notification.builder()
                    .setTitle(message.title)
                    .setBody(message.body)
//                    .setImage(message.imageUrl) // TODO: 푸시 이미지 확인 필요
                    .build()
            )
            .build()
    }

    private fun buildMulticastMessage(
        deviceTokens: List<String>,
        message: PushCommand.MessagePayload,
    ): MulticastMessage {
        return MulticastMessage.builder()
            .addAllTokens(deviceTokens)
            .setNotification(
                Notification.builder()
                    .setTitle(message.title)
                    .setBody(message.body)
//                    .setImage(message.imageUrl) // TODO: 푸시 이미지 확인 필요
                    .build()
            )
            .build()
    }
}