package nexters.tuk.domain.push

import nexters.tuk.contract.push.PushType

class PushDto {
    data class Push(
        val recipients: List<PushRecipient>,
        val pushType: PushType = PushType.GATHERING_NOTIFICATION,
    )
}

data class PushRecipient(
    val memberId: Long,
)