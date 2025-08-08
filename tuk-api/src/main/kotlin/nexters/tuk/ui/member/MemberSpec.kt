package nexters.tuk.ui.member

import io.swagger.v3.oas.annotations.Operation
import nexters.tuk.application.member.dto.response.MemberResponse
import nexters.tuk.contract.ApiResponse

interface MemberSpec {
    @Operation(summary = "회원 탈퇴")
    fun deleteMember(memberId: Long): ApiResponse<Unit>

    @Operation(summary = "회원 조회")
    fun getMember(memberId: Long): ApiResponse<MemberResponse.Profile>
}