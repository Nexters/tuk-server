package nexters.tuk.ui.gathering

import nexters.tuk.application.gathering.GatheringService
import nexters.tuk.application.gathering.GatheringMemberService
import nexters.tuk.application.gathering.GatheringQueryService
import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.request.GatheringQuery
import nexters.tuk.application.gathering.dto.response.GatheringMemberResponse
import nexters.tuk.application.gathering.dto.response.GatheringResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/gatherings")
class GatheringController(
    private val gatheringService: GatheringService,
    private val gatheringQueryService: GatheringQueryService,
    private val gatheringMemberService: GatheringMemberService,
) : GatheringSpec {

    @PostMapping
    override fun generateGathering(
        @Authenticated memberId: Long,
        @RequestBody request: GatheringDto.Request.Generate,
    ): ApiResponse<GatheringResponse.Generate> {

        val response = gatheringService.generateGathering(request.toCommand(memberId))

        return ApiResponse.ok(response)
    }

    @PatchMapping("/{gatheringId}")
    override fun updateGathering(
        @Authenticated memberId: Long,
        @PathVariable gatheringId: Long,
        @RequestBody request: GatheringDto.Request.Update,
    ): ApiResponse<GatheringResponse.Simple> {
        val response = gatheringService.updateGathering(
            GatheringCommand.Update(
                gatheringId = gatheringId,
                gatheringIntervalDays = request.gatheringIntervalDays,
                memberId = memberId
            )
        )

        return ApiResponse.ok(response)
    }

    @GetMapping
    override fun getMemberGathering(
        @Authenticated memberId: Long,
    ): ApiResponse<GatheringResponse.GatheringOverviews> {

        val query = GatheringQuery.MemberGathering(memberId)
        val response = gatheringQueryService.getMemberGatherings(query)

        return ApiResponse.ok(response)
    }

    @GetMapping("/{gatheringId}/members")
    override fun getGatheringDetail(
        @Authenticated memberId: Long,
        @PathVariable("gatheringId") gatheringId: Long,
    ): ApiResponse<GatheringResponse.GatheringDetail> {

        val query = GatheringQuery.GatheringDetail(memberId, gatheringId)
        val response = gatheringQueryService.getGatheringDetail(query)

        return ApiResponse.ok(response)
    }

    @PostMapping("/{gatheringId}/members")
    override fun joinGathering(
        @Authenticated memberId: Long,
        @PathVariable("gatheringId") gatheringId: Long,
    ): ApiResponse<GatheringMemberResponse.JoinGathering> {

        val response = gatheringMemberService.joinGathering(gatheringId, memberId)

        return ApiResponse.ok(response)
    }

    @GetMapping("/{gatheringId}/name")
    override fun getGatheringName(
        @PathVariable("gatheringId") gatheringId: Long,
    ): ApiResponse<GatheringResponse.GatheringName> {

        val response = gatheringQueryService.getGatheringName(gatheringId)

        return ApiResponse.ok(response)
    }

    @DeleteMapping("/{gatheringId}")
    override fun deleteGathering(
        @Authenticated memberId: Long,
        @PathVariable("gatheringId") gatheringId: Long
    ): ApiResponse<Unit> {
        gatheringService.deleteGathering(
            GatheringCommand.Delete(
                memberId = memberId,
                gatheringId = gatheringId
            )
        )

        return ApiResponse.ok()
    }
}