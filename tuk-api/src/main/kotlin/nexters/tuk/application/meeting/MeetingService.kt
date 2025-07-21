package nexters.tuk.application.meeting

import nexters.tuk.application.meeting.dto.request.MeetingCommand
import nexters.tuk.application.meeting.dto.response.MeetingResponse
import nexters.tuk.application.member.MemberService
import nexters.tuk.domain.meeting.Meeting
import nexters.tuk.domain.meeting.MeetingMember
import nexters.tuk.domain.meeting.MeetingMemberRepository
import nexters.tuk.domain.meeting.MeetingRepository
import org.springframework.data.domain.PageRequest
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
        val pageable = PageRequest.of(command.page, command.size)

        val meetingMemberSlice = meetingMemberRepository
            .findAllByMemberOrderByMeetingName(member, pageable)

        val meetingIds = meetingMemberSlice.content.map { it.id }
        val meetings = meetingRepository.findAllById(meetingIds).toList()

        val meetingOverviews = meetings.map { meeting ->
            MeetingResponse.MeetingOverviews.MeetingOverview(
                meetingName = meeting.name,
                monthsSinceLastMeeting = meeting.lastMeetingDate.monthsAgo()
            )
        }

        return MeetingResponse.MeetingOverviews(
            hasNext = meetingMemberSlice.hasNext(),
            currentPage = command.page,
            size = meetings.size,
            meetingOverviews = meetingOverviews
        )
    }

    private fun LocalDate.monthsAgo(): Int {
        val daysBetween = ChronoUnit.DAYS.between(this, LocalDate.now())
        return (daysBetween / 30).toInt()
    }
}