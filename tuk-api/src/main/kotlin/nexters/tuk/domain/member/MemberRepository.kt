package nexters.tuk.domain.member

import nexters.tuk.application.member.SocialType
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?

    fun findBySocialTypeAndSocialId(socialType: SocialType, socialId: String): Member?
}