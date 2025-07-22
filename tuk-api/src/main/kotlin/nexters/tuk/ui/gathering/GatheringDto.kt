package nexters.tuk.ui.gathering

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.gathering.dto.request.GatheringCommand

class GatheringDto {
    class Request {
        data class Generate(
            @Schema(description = "모임명", required = true)
            val gatheringName: String,
            @Schema(description = "현재로부터 마지막 모임까지의 일수", defaultValue = "0")
            val daysSinceLastGathering: Long? = 0,
            @Schema(description = "모임 주기 (일 단위)", defaultValue = "30")
            val gatheringIntervalDays: Long? = DEFAULT_INTERVAL_DAYS,
            @Schema(description = "모임 관련 태그 목록")
            val tags: List<String>? = listOf()
        ) {
            companion object {
                private const val DEFAULT_INTERVAL_DAYS = 30L
                private const val DEFAULT_DAYS_SINCE_LAST_GATHERING = 0L
            }

            fun toCommand(memberId: Long): GatheringCommand.Generate {
                return GatheringCommand.Generate(
                    memberId,
                    gatheringName,
                    daysSinceLastGathering ?: DEFAULT_DAYS_SINCE_LAST_GATHERING,
                    gatheringIntervalDays ?: DEFAULT_INTERVAL_DAYS,
                    tags ?: listOf()
                )
            }
        }
    }
}