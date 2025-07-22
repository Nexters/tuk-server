package nexters.tuk.ui.gathering

import nexters.tuk.application.gathering.GatheringService
import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.config.Authenticated
import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/gathering")
class GatheringController(
    private val gatheringService: GatheringService,
) : GatheringSpec {

    @PostMapping
    override fun generateGathering(
        @Authenticated memberId: Long,
        @RequestBody request: GatheringDto.Request.Generate
    ): ApiResponse<GatheringResponse.Generate> {

        val response = gatheringService.generateGathering(request.toCommand(memberId))

        return ApiResponse.ok(response)
    }

    @GetMapping
    override fun getMemberGathering(
        @Authenticated memberId: Long,
    ): ApiResponse<GatheringResponse.GatheringOverviews> {

        val command = GatheringCommand.GetMemberGathering(memberId)
        val response = gatheringService.getMemberGatherings(command)

        return ApiResponse.ok(response)
    }
}