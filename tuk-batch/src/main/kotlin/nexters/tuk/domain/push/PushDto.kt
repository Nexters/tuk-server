package nexters.tuk.domain.push

import nexters.tuk.contract.push.PushType

class PushDto {
    data class Push(
        val pushType: PushType = PushType.GATHERING_NOTIFICATION,
        val gatheringId: Long,
    )
}