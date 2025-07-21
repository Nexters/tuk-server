package nexters.tuk.domain.meeting

import nexters.tuk.domain.member.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MeetingMemberRepository : JpaRepository<MeetingMember, Long> {
    fun findByMeetingAndMember(meeting: Meeting, member: Member): MeetingMember?

    @Query(
        """
        SELECT meeting_member 
        FROM MeetingMember AS meeting_member
        JOIN FETCH meeting_member.meeting meeting
        WHERE meeting_member.member = :member 
          AND meeting_member.deletedAt IS NULL 
          AND meeting.deletedAt IS NULL
        ORDER BY meeting.name
        """
    )
    fun findAllByMemberOrderByMeetingName(
        @Param("member") member: Member,
    ): List<MeetingMember>
}