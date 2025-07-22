package nexters.tuk.domain.gathering

import nexters.tuk.domain.member.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GatheringMemberRepository : JpaRepository<GatheringMember, Long> {
    fun findByGatheringAndMember(gathering: Gathering, member: Member): GatheringMember?

    @Query(
        """
        SELECT gathering_member 
        FROM GatheringMember AS gathering_member
        JOIN FETCH gathering_member.gathering AS gathering
        WHERE gathering_member.member = :member 
          AND gathering_member.deletedAt IS NULL 
          AND gathering.deletedAt IS NULL
        ORDER BY gathering.name
        """
    )
    fun findAllByMemberOrderByGatheringName(
        @Param("member") member: Member,
    ): List<GatheringMember>
}