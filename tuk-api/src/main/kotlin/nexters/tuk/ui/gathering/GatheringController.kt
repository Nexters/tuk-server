package nexters.tuk.ui.gathering

import nexters.tuk.application.gathering.GatheringAppService
import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.request.GatheringQuery
import nexters.tuk.application.gathering.dto.response.GatheringFacadeResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/gatherings")
class GatheringController(
    private val gatheringAppService: GatheringAppService,
) : GatheringSpec {

    @PostMapping
    override fun generateGathering(
        @Authenticated memberId: Long,
        @RequestBody request: GatheringDto.Request.Generate
    ): ApiResponse<GatheringFacadeResponse.Generate> {

        val response = gatheringAppService.generateGathering(request.toCommand(memberId))

        return ApiResponse.ok(response)
    }

    @GetMapping
    override fun getMemberGathering(
        @Authenticated memberId: Long,
    ): ApiResponse<GatheringFacadeResponse.GatheringOverviews> {

        val query = GatheringQuery.MemberGathering(memberId)
        val response = gatheringAppService.getMemberGatherings(query)

        return ApiResponse.ok(response)
    }

    @GetMapping("/{gatheringId}/members")
    override fun getGatheringDetail(
        @Authenticated memberId: Long,
        @PathVariable("gatheringId") gatheringId: Long
    ): ApiResponse<GatheringFacadeResponse.GatheringDetail> {

        val query = GatheringQuery.GatheringDetail(memberId, gatheringId)
        val response = gatheringAppService.getGatheringDetail(query)

        return ApiResponse.ok(response)
    }

    @PostMapping("/{gatheringId}/members")
    override fun joinGathering(
        @Authenticated memberId: Long,
        @PathVariable("gatheringId") gatheringId: Long
    ): ApiResponse<GatheringFacadeResponse.JoinGathering> {

        val command = GatheringCommand.JoinGathering(memberId, gatheringId)
        val response = gatheringAppService.joinGathering(command)

        return ApiResponse.ok(response)
    }
}