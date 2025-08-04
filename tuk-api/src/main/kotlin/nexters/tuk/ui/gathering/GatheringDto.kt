package nexters.tuk.ui.gathering

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.gathering.dto.request.GatheringCommand

class GatheringDto {
    class Request {
        data class Generate(
            @Schema(description = "모임명", required = true)
            val gatheringName: String,
            @Schema(description = "모임 주기 (일 단위)", defaultValue = "30")
            val gatheringIntervalDays: Long? = DEFAULT_INTERVAL_DAYS,
            @Schema(description = "모임 관련 태그 목록")
            val tagIds: List<Long>? = listOf(),
        ) {
            companion object {
                private const val DEFAULT_INTERVAL_DAYS = 30L
            }

            fun toCommand(memberId: Long): GatheringCommand.Generate {
                return GatheringCommand.Generate(
                    memberId,
                    gatheringName,
                    gatheringIntervalDays ?: DEFAULT_INTERVAL_DAYS,
                    tagIds ?: listOf()
                )
            }
        }

        data class Update(
            @Schema(description = "모임 주기 (일 단위)")
            val gatheringIntervalDays: Long,
        ) {
            fun toCommand(gatheringId: Long): GatheringCommand.Update {
                return GatheringCommand.Update(
                    gatheringId = gatheringId,
                    gatheringIntervalDays = gatheringIntervalDays
                )
            }
        }
    }
}