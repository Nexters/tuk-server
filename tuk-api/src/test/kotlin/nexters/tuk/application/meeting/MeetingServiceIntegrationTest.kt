package nexters.tuk.application.meeting

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import nexters.tuk.application.meeting.dto.request.MeetingCommand
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.member.SocialType
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.domain.meeting.Meeting
import nexters.tuk.domain.meeting.MeetingMember
import nexters.tuk.domain.meeting.MeetingMemberRepository
import nexters.tuk.domain.meeting.MeetingRepository
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.testcontainers.MySqlTestContainersConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@SpringBootTest
class MeetingServiceIntegrationTest @Autowired constructor(
    private val meetingService: MeetingService,
    private val meetingRepository: MeetingRepository,
    private val memberRepository: MemberRepository,
    private val meetingMemberRepository: MeetingMemberRepository,
    @MockkBean private val memberService: MemberService,
) : MySqlTestContainersConfig() {

    @AfterEach
    fun tearDown() {
        meetingMemberRepository.deleteAll()
        meetingRepository.deleteAll()
        memberRepository.deleteAll()
    }

    @Test
    fun `모임 생성시 모임과 관련된 정보들이 정상적으로 저장된다`() {
        // given
        val member = Member.signUp(
            MemberCommand.SignUp(
                socialId = "1",
                socialType = SocialType.GOOGLE,
                email = "test@test.com",
            )
        )
        memberRepository.save(member)
        every { memberService.findById(any()) } returns member

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

    @Test
    fun `유저의 모임 목록을 정상적으로 조회한다`() {
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
        every { memberService.findById(member.id) } returns member

        val meeting1 = meetingRepository.save(
            Meeting.generate(
                member,
                MeetingCommand.Generate(member.id, "meeting1", 0, 7, emptyList())
            )
        )
        val meeting2 = meetingRepository.save(
            Meeting.generate(
                member,
                MeetingCommand.Generate(member.id, "meeting2", 0, 7, emptyList())
            )
        )

        meetingMemberRepository.save(MeetingMember.registerHostMember(meeting1, member))
        meetingMemberRepository.save(MeetingMember.registerHostMember(meeting2, member))

        val command = MeetingCommand.GetMemberMeetings(memberId = member.id)

        // when
        val result = meetingService.getMemberMeetings(command)

        // then
        assertThat(result.meetingOverviews).hasSize(2)
    }

    @Test
    fun `유저에게 모임이 없는 경우 빈 목록을 반환한다`() {
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
        every { memberService.findById(member.id) } returns member

        val command = MeetingCommand.GetMemberMeetings(memberId = member.id)

        // when
        val result = meetingService.getMemberMeetings(command)

        // then
        assertThat(result.meetingOverviews).isEmpty()
    }

    @Test
    fun `다른 유저의 모임은 조회되지 않는다`() {
        // given
        val member1 = memberRepository.save(
            Member.signUp(
                MemberCommand.SignUp(
                    socialId = "1",
                    socialType = SocialType.GOOGLE,
                    email = "test1@test.com",
                )
            )
        )
        val member2 = memberRepository.save(
            Member.signUp(
                MemberCommand.SignUp(
                    socialId = "2",
                    socialType = SocialType.GOOGLE,
                    email = "test2@test.com",
                )
            )
        )
        every { memberService.findById(member1.id) } returns member1

        val meeting1 = meetingRepository.save(
            Meeting.generate(
                member1,
                MeetingCommand.Generate(member1.id, "meeting1", 0, 7, emptyList())
            )
        )
        meetingMemberRepository.save(MeetingMember.registerHostMember(meeting1, member1))

        val meeting2 = meetingRepository.save(
            Meeting.generate(
                member2,
                MeetingCommand.Generate(member2.id, "meeting2", 0, 7, emptyList())
            )
        )
        meetingMemberRepository.save(MeetingMember.registerHostMember(meeting2, member2))

        val command = MeetingCommand.GetMemberMeetings(memberId = member1.id)

        // when
        val result = meetingService.getMemberMeetings(command)

        // then
        assertThat(result.meetingOverviews).hasSize(1)
        assertThat(result.meetingOverviews.first().meetingName).isEqualTo("meeting1")
    }

    @Test
    fun `모임 이름 오름차순으로 정렬되어 조회된다`() {
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
        every { memberService.findById(member.id) } returns member

        val meetingA = meetingRepository.save(
            Meeting.generate(
                member,
                MeetingCommand.Generate(member.id, "A_meeting", 0, 7, emptyList())
            )
        )
        val meetingB = meetingRepository.save(
            Meeting.generate(
                member,
                MeetingCommand.Generate(member.id, "B_meeting", 0, 7, emptyList())
            )
        )
        val meetingC = meetingRepository.save(
            Meeting.generate(
                member,
                MeetingCommand.Generate(member.id, "C_meeting", 0, 7, emptyList())
            )
        )

        meetingMemberRepository.save(MeetingMember.registerHostMember(meetingA, member))
        meetingMemberRepository.save(MeetingMember.registerHostMember(meetingB, member))
        meetingMemberRepository.save(MeetingMember.registerHostMember(meetingC, member))

        val command = MeetingCommand.GetMemberMeetings(memberId = member.id)

        // when
        val result = meetingService.getMemberMeetings(command)

        // then
        assertThat(result.meetingOverviews.map { it.meetingName }).containsExactly(
            "A_meeting",
            "B_meeting",
            "C_meeting"
        )
    }

    @Test
    fun `존재하지 않는 유저로 조회시 예외가 발생한다`() {
        // given
        every { memberService.findById(any()) } throws RuntimeException("Member not found")

        val command = MeetingCommand.GetMemberMeetings(memberId = 999L)

        // when, then
        assertThrows<RuntimeException> {
            meetingService.getMemberMeetings(command)
        }
    }

    @Test
    fun `200개의 모임 데이터가 순서대로 잘 저장되었는지 확인한다`() {
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
        every { memberService.findById(member.id) } returns member

        val meetings = (1..200).map {
            val meetingName = "meeting" + String.format("%03d", it)
            val meeting = Meeting.generate(member, MeetingCommand.Generate(member.id, meetingName, 0, 7, emptyList()))
            meetingRepository.save(meeting)
        }.shuffled()

        meetings.forEach {
            meetingMemberRepository.save(MeetingMember.registerHostMember(it, member))
        }

        val command = MeetingCommand.GetMemberMeetings(memberId = member.id)

        // when
        val result = meetingService.getMemberMeetings(command)

        // then
        assertThat(result.meetingOverviews).hasSize(200)
        assertThat(result.meetingOverviews.map { it.meetingName }).isSorted()
    }
}
