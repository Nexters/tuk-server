package nexters.tuk.application.push.dto.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.contract.push.PushType

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

        @Schema(title = "모임 정기 푸시 (GATHERING_NOTIFICATION)")
        data class GatheringNotification(
            val gatheringId: Long,
            override val pushType: PushType = PushType.GATHERING_NOTIFICATION,
        ) : Push

        @Schema(title = "초대장 푸시 (PROPOSAL)")
        data class Proposal(
            override val pushType: PushType = PushType.PROPOSAL,
            val gatheringId: Long,
            val proposalId: Long,
        ) : Push
    }

    data class MessagePayload(
        val title: String,
        val body: String,
        val deepLink: String,
    )
}