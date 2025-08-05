package nexters.tuk.application.push


enum class PushMessage(
    private val title: String,
    val body: String,
) {
    PUSH_VERSION_A("%s님! 방금 '툭'— 누군가 만남을 제안했어요.", "슬슬 그리워질 타이밍... 아닐까요?"),
    PUSH_VERSION_B("누군가 %s님을 떠올리며 툭— 건넸어요.", "이번엔 그냥 지나치지 마세요 :)"),
    PUSH_VERSION_C("%s님, 툭— 누가 당신을 부르고 있어요.", "이번엔 누굴까? 살짝 들여다볼래요?"),
    PUSH_VERSION_D("%s님, 누가 몰래 '툭' 했대요.", "그냥 넘어가긴... 좀 아쉽죠?"),
    ;

    fun getTitle(memberName: String): String {
        return title.format(memberName)
    }

    companion object {
        fun random(): PushMessage {
            return entries.toTypedArray().random()
        }
    }
}