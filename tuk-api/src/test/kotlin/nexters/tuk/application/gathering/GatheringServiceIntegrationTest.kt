package nexters.tuk.application.gathering

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.member.SocialType
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
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
class GatheringServiceIntegrationTest @Autowired constructor(
    private val gatheringService: GatheringService,
    private val gatheringRepository: GatheringRepository,
    private val memberRepository: MemberRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
    @MockkBean private val memberService: MemberService,
) : MySqlTestContainersConfig() {

    @AfterEach
    fun tearDown() {
        gatheringMemberRepository.deleteAll()
        gatheringRepository.deleteAll()
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

        val command = GatheringCommand.Generate(
            memberId = member.id,
            gatheringName = "test gathering",
            daysSinceLastGathering = 10,
            gatheringIntervalDays = 7,
            tags = listOf("tag1", "tag2")
        )

        // when
        val actual = gatheringService.generateGathering(command)

        // then
        val gathering = gatheringRepository.findById(actual.gatheringId).get()
        val gatheringMember = gatheringMemberRepository.findByGatheringAndMember(gathering, member)
        assertAll(
            { assertThat(actual.gatheringId).isNotNull() },
            { assertThat(gathering.name).isEqualTo("test gathering") },
            { assertThat(gathering.firstGatheringDate).isEqualTo(LocalDate.now().minusDays(10)) },
            { assertThat(gathering.lastGatheringDate).isEqualTo(LocalDate.now().minusDays(10)) },
            { assertThat(gathering.intervalDays).isEqualTo(7) },
            { assertThat(gathering.hostMember.id).isEqualTo(member.id) },
            { assertThat(gathering.tags).containsExactly("tag1", "tag2") },
            { assertThat(gatheringMember).isNotNull() },
            { assertThat(gatheringMember!!.isHost).isTrue() }
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

        val gathering1 = gatheringRepository.save(
            Gathering.generate(
                member,
                GatheringCommand.Generate(member.id, "gathering1", 0, 7, emptyList())
            )
        )
        val gathering2 = gatheringRepository.save(
            Gathering.generate(
                member,
                GatheringCommand.Generate(member.id, "gathering2", 0, 7, emptyList())
            )
        )

        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering1, member))
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering2, member))

        val command = GatheringCommand.GetMemberGathering(memberId = member.id)

        // when
        val result = gatheringService.getMemberGatherings(command)

        // then
        assertThat(result.gatheringOverviews).hasSize(2)
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

        val command = GatheringCommand.GetMemberGathering(memberId = member.id)

        // when
        val result = gatheringService.getMemberGatherings(command)

        // then
        assertThat(result.gatheringOverviews).isEmpty()
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

        val gathering1 = gatheringRepository.save(
            Gathering.generate(
                member1,
                GatheringCommand.Generate(member1.id, "gathering1", 0, 7, emptyList())
            )
        )
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering1, member1))

        val gathering2 = gatheringRepository.save(
            Gathering.generate(
                member2,
                GatheringCommand.Generate(member2.id, "gathering2", 0, 7, emptyList())
            )
        )
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering2, member2))

        val command = GatheringCommand.GetMemberGathering(memberId = member1.id)

        // when
        val result = gatheringService.getMemberGatherings(command)

        // then
        assertThat(result.gatheringOverviews).hasSize(1)
        assertThat(result.gatheringOverviews.first().gatheringName).isEqualTo("gathering1")
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

        val gatheringA = gatheringRepository.save(
            Gathering.generate(
                member,
                GatheringCommand.Generate(member.id, "A_gathering", 0, 7, emptyList())
            )
        )
        val gatheringB = gatheringRepository.save(
            Gathering.generate(
                member,
                GatheringCommand.Generate(member.id, "B_gathering", 0, 7, emptyList())
            )
        )
        val gatheringC = gatheringRepository.save(
            Gathering.generate(
                member,
                GatheringCommand.Generate(member.id, "C_gathering", 0, 7, emptyList())
            )
        )

        gatheringMemberRepository.save(GatheringMember.registerHostMember(gatheringA, member))
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gatheringB, member))
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gatheringC, member))

        val command = GatheringCommand.GetMemberGathering(memberId = member.id)

        // when
        val result = gatheringService.getMemberGatherings(command)

        // then
        assertThat(result.gatheringOverviews.map { it.gatheringName }).containsExactly(
            "A_gathering",
            "B_gathering",
            "C_gathering"
        )
    }

    @Test
    fun `존재하지 않는 유저로 조회시 예외가 발생한다`() {
        // given
        every { memberService.findById(any()) } throws RuntimeException("Member not found")

        val command = GatheringCommand.GetMemberGathering(memberId = 999L)

        // when, then
        assertThrows<RuntimeException> {
            gatheringService.getMemberGatherings(command)
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

        val gatherings = (1..200).map {
            val gatheringName = "gathering" + String.format("%03d", it)
            val gathering = Gathering.generate(member, GatheringCommand.Generate(member.id, gatheringName, 0, 7, emptyList()))
            gatheringRepository.save(gathering)
        }.shuffled()

        gatherings.forEach {
            gatheringMemberRepository.save(GatheringMember.registerHostMember(it, member))
        }

        val command = GatheringCommand.GetMemberGathering(memberId = member.id)

        // when
        val result = gatheringService.getMemberGatherings(command)

        // then
        assertThat(result.gatheringOverviews).hasSize(200)
        assertThat(result.gatheringOverviews.map { it.gatheringName }).isSorted()
    }
}
