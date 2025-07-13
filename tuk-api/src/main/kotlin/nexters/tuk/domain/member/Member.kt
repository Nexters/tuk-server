package nexters.tuk.domain.member

import jakarta.persistence.Entity
import jakarta.persistence.Table
import nexters.tuk.application.member.SocialType
import nexters.tuk.application.member.dto.MemberCommand
import nexters.tuk.domain.BaseEntity

@Entity
@Table(name = "member")
class Member private constructor(
    val name: String?,
    val email: String,
    val socialType: SocialType,
    val socialId: String,
) : BaseEntity() {
    companion object {
        fun signUp(command: MemberCommand.SignUp): Member {
            return Member(
                name = null,
                email = command.email,
                socialType = command.socialType,
                socialId = command.socialId,
            )
        }
    }
}