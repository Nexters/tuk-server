package nexters.tuk.ui.member

import nexters.tuk.application.member.MemberService
import nexters.tuk.application.member.dto.response.MemberResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/members")
class MemberController(
    private val memberService: MemberService
) : MemberSpec {

    @PostMapping("/me/onboarding")
    override fun executeOnboarding(
        @Authenticated memberId: Long,
        @RequestBody request: MemberDto.Request.Onboarding
    ): ApiResponse<MemberResponse.Onboarding> {
        val response = memberService.executeOnboarding(request.toCommand(memberId))

        return ApiResponse.ok(response)
    }
}