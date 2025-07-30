package nexters.tuk.ui.onboarding

import nexters.tuk.application.onboarding.OnboardingService
import nexters.tuk.application.onboarding.dto.response.OnboardingResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1/onboarding")
class OnboardingController(
    private val onboardingService: OnboardingService
) : OnboardingSpec {

    @GetMapping
    override fun getRequiredFields(@Authenticated memberId: Long): ApiResponse<OnboardingResponse.RequiredFields> {
        val response = onboardingService.getRequiredFields(memberId)

        return ApiResponse.ok(response)
    }

    @PatchMapping
    override fun initInfo(
        @Authenticated memberId: Long,
        @RequestBody request: OnboardingDto.Request.Init
    ): ApiResponse<OnboardingResponse.Init> {
        val response = onboardingService.initInfo(request.toCommand(memberId))

        return ApiResponse.ok(response)
    }
}