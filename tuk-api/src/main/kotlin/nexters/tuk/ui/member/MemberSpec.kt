package nexters.tuk.ui.member

import io.swagger.v3.oas.annotations.Operation
import nexters.tuk.contract.ApiResponse

interface MemberSpec {
    @Operation(summary = "회원 탈퇴")
    fun deleteMember(memberId: Long) : ApiResponse<Unit>
}