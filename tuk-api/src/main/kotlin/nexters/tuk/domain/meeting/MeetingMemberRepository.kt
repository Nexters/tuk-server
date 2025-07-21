package nexters.tuk.domain.meeting

import nexters.tuk.domain.member.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MeetingMemberRepository : JpaRepository<MeetingMember, Long> {
    fun findByMeetingAndMember(meeting: Meeting, member: Member): MeetingMember?
}