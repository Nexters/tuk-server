package nexters.tuk.ui.meeting

import io.swagger.v3.oas.annotations.media.Schema
import nexters.tuk.application.meeting.dto.request.MeetingCommand

class MeetingDto {
    class Request {
        data class Generate(
            @Schema(description = "모임명", required = true)
            val meetingName: String,
            @Schema(description = "현재로부터 마지막 모임까지의 일수", defaultValue = "0")
            val daysSinceLastMeeting: Long? = 0,
            @Schema(description = "모임 주기 (일 단위)", defaultValue = "30")
            val meetingIntervalDays: Long? = DEFAULT_INTERVAL_DAYS,
            @Schema(description = "모임 관련 태그 목록")
            val tags: List<String>? = listOf()
        ) {
            companion object {
                private const val DEFAULT_INTERVAL_DAYS = 30L
                private const val DEFAULT_DAYS_SINCE_LAST_MEETING = 0L
            }

            fun toCommand(memberId: Long): MeetingCommand.Generate {
                return MeetingCommand.Generate(
                    memberId,
                    meetingName,
                    daysSinceLastMeeting ?: DEFAULT_DAYS_SINCE_LAST_MEETING,
                    meetingIntervalDays ?: DEFAULT_INTERVAL_DAYS,
                    tags ?: listOf()
                )
            }
        }
    }
}