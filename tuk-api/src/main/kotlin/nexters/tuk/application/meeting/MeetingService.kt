package nexters.tuk.application.meeting

import nexters.tuk.application.meeting.dto.request.MeetingCommand
import nexters.tuk.application.meeting.dto.response.MeetingResponse
import nexters.tuk.application.member.MemberService
import nexters.tuk.domain.meeting.Meeting
import nexters.tuk.domain.meeting.MeetingMember
import nexters.tuk.domain.meeting.MeetingMemberRepository
import nexters.tuk.domain.meeting.MeetingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class MeetingService(
    private val meetingRepository: MeetingRepository,
    private val meetingMemberRepository: MeetingMemberRepository,
    private val memberService: MemberService,
) {
    @Transactional
    fun generateMeeting(command: MeetingCommand.Generate): MeetingResponse.Generate {
        val member = memberService.findById(command.memberId)

        val meeting = Meeting.generate(member, command)
        val savedMeeting = meetingRepository.save(meeting)

        val meetingMember = MeetingMember.registerHostMember(savedMeeting, member)
        meetingMemberRepository.save(meetingMember)

        // TODO 알림 등록하기
        return MeetingResponse.Generate(
            meetingId = savedMeeting.id
        )
    }

    @Transactional(readOnly = true)
    fun getMemberMeetings(command: MeetingCommand.GetMemberMeetings): MeetingResponse.MeetingOverviews {
        val member = memberService.findById(command.memberId)

        val meetingMember = meetingMemberRepository.findAllByMemberOrderByMeetingName(member)
        val overViews = meetingMember
            .map { it.meeting }
            .map {
                MeetingResponse.MeetingOverviews.MeetingOverview(
                    it.name,
                    it.lastMeetingDate.monthsAgo()
                )
            }

        return MeetingResponse.MeetingOverviews(
            size = overViews.size,
            meetingOverviews = overViews
        )
    }

    private fun LocalDate.monthsAgo(): Int {
        val daysBetween = ChronoUnit.DAYS.between(this, LocalDate.now())
        return (daysBetween / 30).toInt()
    }
}