package nexters.tuk.domain.gathering

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GatheringMemberRepository : JpaRepository<GatheringMember, Long> {
    fun findByGatheringAndMemberId(gathering: Gathering, memberId: Long): GatheringMember?

    @Query(
        """
        SELECT gathering_member 
        FROM GatheringMember AS gathering_member
        JOIN FETCH gathering_member.gathering AS gathering
        WHERE gathering_member.memberId = :memberId 
          AND gathering_member.deletedAt IS NULL 
          AND gathering.deletedAt IS NULL
        """
    )
    fun findAllByMemberId(
        @Param("memberId") memberId: Long,
    ): List<GatheringMember>


    fun findAllByGathering(gathering: Gathering): List<GatheringMember>
}