package nexters.tuk.ui.gathering

import nexters.tuk.application.gathering.GatheringService
import nexters.tuk.application.gathering.dto.request.GatheringQuery
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.config.Authenticated
import nexters.tuk.contract.ApiResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/gatherings")
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

        val query = GatheringQuery.MemberGathering(memberId)
        val response = gatheringService.getMemberGatherings(query)

        return ApiResponse.ok(response)
    }

    @GetMapping("/{gatheringId}/members")
    override fun getGatheringDetail(
        memberId: Long,
        @PathVariable("gatheringId") gatheringId: Long
    ): ApiResponse<GatheringResponse.GatheringDetail> {

        val query = GatheringQuery.GatheringDetail(memberId, gatheringId)
        val response = gatheringService.getGatheringDetail(query)

        return ApiResponse.ok(response)
    }
}