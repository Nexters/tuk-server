package nexters.tuk.ui.meeting

import nexters.tuk.application.meeting.MeetingService
import nexters.tuk.application.meeting.dto.request.MeetingCommand
import nexters.tuk.application.meeting.dto.response.MeetingResponse
import nexters.tuk.config.Authenticated
import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/meeting")
class MeetingController(
    private val meetingService: MeetingService,
) : MeetingSpec {

    @PostMapping
    override fun generateMeeting(
        @Authenticated memberId: Long,
        @RequestBody request: MeetingDto.Request.Generate
    ): ApiResponse<MeetingResponse.Generate> {

        val response = meetingService.generateMeeting(request.toCommand(memberId))

        return ApiResponse.ok(response)
    }

    @GetMapping
    override fun getMemberMeetings(
        @Authenticated memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<MeetingResponse.MeetingOverviews> {

        val command = MeetingCommand.GetMemberMeetings(memberId, page, size)
        val response = meetingService.getMemberMeetings(command)

        return ApiResponse.ok(response)
    }
}