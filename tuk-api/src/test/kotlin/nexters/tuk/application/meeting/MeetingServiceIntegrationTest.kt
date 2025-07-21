package nexters.tuk.application.meeting

import nexters.tuk.application.meeting.dto.request.MeetingCommand
import nexters.tuk.application.member.SocialType
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.domain.meeting.MeetingMemberRepository
import nexters.tuk.domain.meeting.MeetingRepository
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.testcontainers.MySqlTestContainersConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class MeetingServiceIntegrationTest @Autowired constructor(
    private val meetingService: MeetingService,
    private val meetingRepository: MeetingRepository,
    private val memberRepository: MemberRepository,
    private val meetingMemberRepository: MeetingMemberRepository,
) : MySqlTestContainersConfig() {

    @AfterEach
    fun tearDown() {
        meetingRepository.deleteAll()
        memberRepository.deleteAll()
    }

    @Test
    fun `모임 생성시 모임과 관련된 정보들이 정상적으로 저장된다`() {
        // given
        val member = memberRepository.save(
            Member.signUp(
                MemberCommand.SignUp(
                    socialId = "1",
                    socialType = SocialType.GOOGLE,
                    email = "test@test.com",
                )
            )
        )

        val command = MeetingCommand.Generate(
            memberId = member.id,
            meetingName = "test meeting",
            daysSinceLastMeeting = 10,
            meetingIntervalDays = 7,
            tags = listOf("tag1", "tag2")
        )

        // when
        val actual = meetingService.generateMeeting(command)

        // then
        val meeting = meetingRepository.findById(actual.meetingId).get()
        val meetingMember = meetingMemberRepository.findByMeetingAndMember(meeting, member)
        assertAll(
            { assertThat(actual.meetingId).isNotNull() },
            { assertThat(meeting.name).isEqualTo("test meeting") },
            { assertThat(meeting.firstMeetingDate).isEqualTo(LocalDate.now().minusDays(10)) },
            { assertThat(meeting.lastMeetingDate).isEqualTo(LocalDate.now().minusDays(10)) },
            { assertThat(meeting.intervalDays).isEqualTo(7) },
            { assertThat(meeting.hostMember.id).isEqualTo(member.id) },
            { assertThat(meeting.tags).containsExactly("tag1", "tag2") },
            { assertThat(meetingMember).isNotNull() },
            { assertThat(meetingMember!!.isHost).isTrue() }
        )
    }
}
