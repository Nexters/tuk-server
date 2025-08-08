package nexters.tuk.ui.member

import nexters.tuk.application.member.MemberService
import nexters.tuk.application.member.dto.response.MemberResponse
import nexters.tuk.contract.ApiResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val memberService: MemberService,
) : MemberSpec {

    @DeleteMapping()
    override fun deleteMember(
        @Authenticated memberId: Long,
    ): ApiResponse<Unit> {
        memberService.deleteMember(memberId)

        return ApiResponse.ok()
    }

    @GetMapping("/me")
    override fun getMember(
        @Authenticated memberId: Long,
    ): ApiResponse<MemberResponse.Profile> {
        val response = memberService.getMemberProfile(memberId)

        return ApiResponse.ok(response)
    }

    @PatchMapping
    override fun updateMember(
        @Authenticated memberId: Long,
        @RequestBody request: MemberDto.Request.Update
    ): ApiResponse<MemberResponse.Profile> {
        val response = memberService.updateProfile(request.toCommand(memberId))

        return ApiResponse.ok(response)
    }
}