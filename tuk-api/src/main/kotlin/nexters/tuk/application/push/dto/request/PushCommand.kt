package nexters.tuk.application.push.dto.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.push.PushType

class PushCommand {
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "pushType"
    )
    @JsonSubTypes(
        JsonSubTypes.Type(value = Push.GatheringNotification::class, name = "GATHERING_NOTIFICATION"),
        JsonSubTypes.Type(value = Push.Proposal::class, name = "PROPOSAL")
    )
    sealed interface Push {
        val pushType: PushType
        val message: MessagePayload

        @Schema(title = "모임 정기 푸시 (GATHERING_NOTIFICATION)")
        data class GatheringNotification(
            val recipients: List<PushRecipient>,
            override val pushType: PushType = PushType.GATHERING_NOTIFICATION,
            override val message: MessagePayload,
        ) : Push

        @Schema(title = "초대장 푸시 (PROPOSAL)")
        data class Proposal(
            override val pushType: PushType = PushType.PROPOSAL,
            override val message: MessagePayload,
            val gatheringId: Long,
        ) : Push
    }

    data class MessagePayload(
        val title: String,
        val body: String,
    )

    data class PushRecipient(
        val memberId: Long,
    )
}