package nexters.tuk.domain.member

import nexters.tuk.application.member.SocialType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface MemberRepository : JpaRepository<Member, Long> {
    fun findBySocialTypeAndSocialId(socialType: SocialType, socialId: String): Member?

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Member where id =:memberId")
    fun leave(memberId: Long)
}