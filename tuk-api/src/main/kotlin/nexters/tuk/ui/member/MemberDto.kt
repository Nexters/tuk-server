package nexters.tuk.ui.member

import nexters.tuk.application.member.dto.request.MemberCommand

class MemberDto {
    class Request {
        data class Onboarding(
            val name: String
        ) {
            fun toCommand(memberId: Long): MemberCommand.Onboarding {
                return MemberCommand.Onboarding(
                    memberId = memberId,
                    name = name
                )
            }
        }
    }
}