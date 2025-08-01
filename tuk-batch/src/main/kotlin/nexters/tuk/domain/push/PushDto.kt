package nexters.tuk.domain.push

class PushDto {
    data class Push(
        val recipients: List<PushRecipient>,
        val message: MessagePayload,
        val pushType: PushType,
    )

    data class MessagePayload(
        val title: String,
        val body: String,
    )

    data class PushRecipient(
        val deviceToken: String,
        val memberId: Long,
    )

    enum class PushType {
        // 모임 정기 푸시
        GROUP_NOTIFICATION,

        // 초대장 푸시
        INVITATION,
    }
}
