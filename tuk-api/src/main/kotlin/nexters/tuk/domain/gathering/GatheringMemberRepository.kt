package nexters.tuk.domain.gathering

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GatheringMemberRepository : JpaRepository<GatheringMember, Long> {
    fun findByGatheringAndMemberId(gathering: Gathering, memberId: Long): GatheringMember?

    @Query(
        """
        SELECT gathering_member 
        FROM GatheringMember gathering_member
        JOIN FETCH gathering_member.gathering  gathering
        WHERE gathering_member.memberId = :memberId 
          AND gathering_member.deletedAt IS NULL 
          AND gathering.deletedAt IS NULL
        """
    )
    fun findAllByMemberId(
        @Param("memberId") memberId: Long,
    ): List<GatheringMember>


    fun findAllByGathering(gathering: Gathering): List<GatheringMember>

    @Modifying
    @Query(
        """
        DELETE FROM GatheringMember gm
        WHERE gm.memberId = :memberId
        """
    )
    fun deleteAllByMemberId(memberId: Long)
}