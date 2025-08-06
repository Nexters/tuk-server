package nexters.tuk.ui.member

import nexters.tuk.application.member.MemberService
import nexters.tuk.contract.ApiResponse
import nexters.tuk.ui.resolver.Authenticated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}