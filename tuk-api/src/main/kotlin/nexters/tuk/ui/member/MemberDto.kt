package nexters.tuk.ui.member

import nexters.tuk.application.member.dto.request.MemberCommand

class MemberDto {
    class Request {
        data class Update(val name: String?) {
            fun toCommand(memberId: Long): MemberCommand.UpdateProfile {
                return MemberCommand.UpdateProfile(
                    memberId = memberId,
                    name = name
                )
            }
        }
    }
}