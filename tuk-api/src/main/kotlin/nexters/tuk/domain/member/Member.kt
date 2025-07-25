package nexters.tuk.domain.member

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import nexters.tuk.application.member.SocialType
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.domain.BaseEntity
import org.hibernate.annotations.SQLRestriction

/**
 * FIXME: member 임시 테이블
 */
@SQLRestriction("deleted_at is NULL")
@Entity
@Table(name = "member")
class Member private constructor(
    val name: String?,
    val email: String,
    @Enumerated(EnumType.STRING)
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