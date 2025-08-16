package nexters.tuk.application.push

import nexters.tuk.contract.push.PushType

enum class PushMessage(
    val pushType: PushType,
    private val title: String,
    val body: String,
    val deepLink: PushDeepLink,
) {
    PUSH_VERSION_A(
        PushType.GATHERING_NOTIFICATION,
        "%s님! 방금 '툭'— 누군가 만남을 제안했어요.",
        "슬슬 그리워질 타이밍... 아닐까요?",
        PushDeepLink.DEFAULT,
    ),
    PUSH_VERSION_B(
        PushType.GATHERING_NOTIFICATION,
        "누군가 %s님을 떠올리며 툭— 건넸어요.",
        "이번엔 그냥 지나치지 마세요 :)",
        PushDeepLink.DEFAULT,
    ),
    PUSH_VERSION_C(
        PushType.GATHERING_NOTIFICATION,
        "%s님, 툭— 누가 당신을 부르고 있어요.",
        "이번엔 누굴까? 살짝 들여다볼래요?",
        PushDeepLink.DEFAULT,
    ),
    PUSH_VERSION_D(
        PushType.GATHERING_NOTIFICATION,
        "%s님, 누가 몰래 '툭' 했대요.",
        "그냥 넘어가긴... 좀 아쉽죠?",
        PushDeepLink.DEFAULT,
    ),

    PROPOSAL(
        PushType.PROPOSAL,
        "%s님! 방금 '툭'— 누군가 만남을 제안했어요.",
        "슬슬 그리워질 타이밍... 아닐까요?",
        PushDeepLink.PROPOSAL,
    )
    ;


    fun getTitle(memberName: String): String {
        return title.format(memberName)
    }

    companion object {
        fun random(proposalId: Long? = null): PushData {
            return if (proposalId != null) {
                PushData(PROPOSAL, Meta(proposalId))
            } else {
                val randomMessage = entries.filter { it.pushType == PushType.GATHERING_NOTIFICATION }.random()
                PushData(randomMessage, null)
            }
        }
    }
}

data class PushData(
    val message: PushMessage,
    val meta: Meta? = null,
) {
    fun getTitle(memberName: String): String {
        return message.getTitle(memberName)
    }

    val body: String
        get() = message.body

    val deepLink: String
        get() = message.deepLink.link
}

data class Meta(
    val proposalId: Long,
)