package nexters.tuk.domain.meeting

import jakarta.persistence.*
import nexters.tuk.application.meeting.dto.request.MeetingCommand
import nexters.tuk.domain.BaseEntity
import nexters.tuk.domain.member.Member
import nexters.tuk.infrastructure.jpa.StringListConverter
import java.time.LocalDate

@Entity
@Table(name = "meeting")
class Meeting private constructor(
    @Column(name = "meeting_name", nullable = false)
    val name: String,

    @Column(name = "first_meeting_date", nullable = false, updatable = false)
    val firstMeetingDate: LocalDate,

    @Column(name = "last_meeting_date", nullable = false, updatable = false)
    val lastMeetingDate: LocalDate,

    @Column(name = "interval_days", nullable = false)
    val intervalDays: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_member_id", nullable = false, updatable = false)
    val hostMember: Member,

    @Convert(converter = StringListConverter::class)
    @Column(name = "tags", columnDefinition = "json")
    val tags: List<String> = listOf()

) : BaseEntity() {
    companion object {
        fun generate(member: Member, command: MeetingCommand.Generate): Meeting {
            return Meeting(
                hostMember = member,
                name = command.meetingName,
                firstMeetingDate = LocalDate.now().minusDays(command.daysSinceLastMeeting),
                lastMeetingDate = LocalDate.now().minusDays(command.daysSinceLastMeeting),
                intervalDays = command.meetingIntervalDays,
                tags = command.tags,
            )
        }
    }
}

