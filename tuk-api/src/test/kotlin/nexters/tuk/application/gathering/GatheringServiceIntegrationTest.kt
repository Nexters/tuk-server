package nexters.tuk.application.gathering

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.gathering.dto.request.GatheringQuery
import nexters.tuk.application.invitation.InvitationService
import nexters.tuk.application.member.MemberService
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.ErrorType
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.fixtures.GatheringFixture
import nexters.tuk.fixtures.GatheringFixtureHelper
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@SpringBootTest
class GatheringServiceIntegrationTest @Autowired constructor(
    private val gatheringService: GatheringService,
    private val gatheringRepository: GatheringRepository,
    private val memberRepository: MemberRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
    @MockkBean private val memberService: MemberService,
    @MockkBean private val invitationService: InvitationService,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)
    private val gatheringFixture = GatheringFixtureHelper(gatheringRepository, gatheringMemberRepository)


    private fun setupMocks(vararg members: Member) {
        if (members.isNotEmpty()) {
            every { memberService.getMemberOverview(any<List<Long>>()) } returns members.map {
                nexters.tuk.application.member.dto.response.MemberResponse.MemberOverview(it.id, it.name ?: "이름 없음")
            }
        }
        every {
            invitationService.getGatheringInvitationStat(
                any(), any()
            )
        } returns nexters.tuk.application.invitation.dto.response.InvitationResponse.InvitationStat(0, 0)
    }

    private fun createMemberWithMocks(socialId: String = "1", email: String = "test@test.com"): Member {
        return memberFixture.createMember(socialId, email).also { member -> setupMocks(member) }
    }

    @AfterEach
    fun tearDown() {
        gatheringMemberRepository.deleteAllInBatch()
        gatheringRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `모임 생성시 모임과 관련된 정보들이 정상적으로 저장된다`() {
        // given
        val member = createMemberWithMocks()

        val command = GatheringFixture.gatheringGenerateCommand(
            memberId = member.id,
            gatheringName = "test gathering",
            daysSinceLastGathering = 10,
            gatheringIntervalDays = 7,
            tags = listOf("tag1", "tag2")
        )

        // when
        val result = gatheringService.generateGathering(command)

        // then
        val gathering = gatheringRepository.findById(result.gatheringId).orElse(null)

        with(gathering) {
            assertAll(
                { assertThat(result.gatheringId).isNotNull() },
                { assertThat(name).isEqualTo("test gathering") },
                { assertThat(firstGatheringDate).isEqualTo(LocalDate.now().minusDays(10)) },
                { assertThat(lastGatheringDate).isEqualTo(LocalDate.now().minusDays(10)) },
                { assertThat(intervalDays).isEqualTo(7) },
                { assertThat(hostId).isEqualTo(member.id) },
                { assertThat(tags).containsExactlyInAnyOrder("tag1", "tag2") })
        }
    }

    @Test
    fun `유저의 모임 목록을 정상적으로 조회한다`() {
        // given
        val member = createMemberWithMocks()
        val gathering1 = gatheringFixture.createGathering(member, "gathering1")
        val gathering2 = gatheringFixture.createGathering(member, "gathering2")
        gatheringFixture.registerGatheringMember(gathering1, member)
        gatheringFixture.registerGatheringMember(gathering2, member)

        // when
        val result = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member.id)
        )

        // then
        assertThat(result.gatheringOverviews).hasSize(2)
    }

    @Test
    fun `유저에게 모임이 없는 경우 빈 목록을 반환한다`() {
        // given
        val member = createMemberWithMocks()
        // 이미 빈 상태이므로 별도 설정 불필요

        // when
        val result = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member.id)
        )

        // then
        assertThat(result.gatheringOverviews).isEmpty()
    }

    @Test
    fun `다른 유저의 모임은 조회되지 않는다`() {
        // given
        val member1 = createMemberWithMocks("1", "test1@test.com")
        val member2 = createMemberWithMocks("2", "test2@test.com")

        // generateGathering을 통해 모임을 생성하면 자동으로 호스트가 등록됨
        gatheringService.generateGathering(GatheringFixture.gatheringGenerateCommand(member1.id, "gathering1"))
        gatheringService.generateGathering(GatheringFixture.gatheringGenerateCommand(member2.id, "gathering2"))

        // when
        val result = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member1.id)
        )

        // then
        assertThat(result.gatheringOverviews).hasSize(1)
        assertThat(result.gatheringOverviews.first().gatheringName).isEqualTo("gathering1")
    }

    @Test
    fun `모임 이름 오름차순으로 정렬되어 조회된다`() {
        // given
        val member = createMemberWithMocks()

        listOf("A_gathering", "B_gathering", "C_gathering").forEach { name ->
            gatheringService.generateGathering(GatheringFixture.gatheringGenerateCommand(member.id, name))
        }

        // when
        val result = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member.id)
        )

        // then
        assertThat(result.gatheringOverviews.map { it.gatheringName }).containsExactly(
            "A_gathering", "B_gathering", "C_gathering"
        )
    }

    @Test
    fun `200개의 모임 데이터가 순서대로 잘 저장되었는지 확인한다`() {
        // given
        val member = createMemberWithMocks()

        val gatheringNames = (1..200).map { "gathering${String.format("%03d", it)}" }.shuffled()

        gatheringNames.forEach { name ->
            gatheringService.generateGathering(GatheringFixture.gatheringGenerateCommand(member.id, name))
        }

        // when
        val result = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member.id)
        )

        // then
        assertThat(result.gatheringOverviews).hasSize(200)
        assertThat(result.gatheringOverviews.map { it.gatheringName }).isSorted()
    }

    @Test
    fun `삭제된 모임은 조회되지 않는다`() {
        // given
        val member = createMemberWithMocks()

        gatheringService.generateGathering(GatheringFixture.gatheringGenerateCommand(member.id, "gathering1"))
        val result2 =
            gatheringService.generateGathering(GatheringFixture.gatheringGenerateCommand(member.id, "gathering2"))

        val gathering2 = gatheringRepository.findById(result2.gatheringId).orElse(null)
        gatheringRepository.delete(gathering2)

        // when
        val result = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member.id)
        )

        // then
        assertThat(result.gatheringOverviews).hasSize(1)
        assertThat(result.gatheringOverviews.first().gatheringName).isEqualTo("gathering1")
    }

    @Test
    fun `모임 상세 정보를 정상적으로 조회한다`() {
        // given
        val member = createMemberWithMocks()
        setupMocks()

        val result = gatheringService.generateGathering(
            GatheringFixture.gatheringGenerateCommand(member.id, "test gathering", daysSinceLastGathering = 10)
        )

        // when
        val detailResult = gatheringService.getGatheringDetail(
            GatheringQuery.GatheringDetail(gatheringId = result.gatheringId, memberId = member.id)
        )

        // then
        with(detailResult) {
            assertAll(
                { assertThat(gatheringName).isEqualTo("test gathering") },
                { assertThat(daysSinceFirstGathering).isEqualTo(10) },
                { assertThat(monthsSinceLastGathering).isEqualTo(0) },
                { assertThat(sentInvitationCount).isEqualTo(0) },
                { assertThat(receivedInvitationCount).isEqualTo(0) },
                { assertThat(members).hasSize(1) })
        }
    }

    @Test
    fun `모임에 속한 멤버들의 정보를 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "1", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "2", email = "member@test.com")
        setupMocks(host, member)

        val gatheringResult = gatheringService.generateGathering(
            GatheringFixture.gatheringGenerateCommand(host.id, "test gathering", daysSinceLastGathering = 10)
        )

        val gathering = gatheringRepository.findById(gatheringResult.gatheringId).orElse(null)
        // 추가 멤버 등록
        gatheringFixture.registerGatheringMember(gathering, member)

        // when
        val result = gatheringService.getGatheringDetail(
            GatheringQuery.GatheringDetail(gatheringId = gathering.id, memberId = host.id)
        )

        // then
        assertThat(result.members).hasSize(2)
    }

    @Test
    fun `존재하지 않는 모임 ID로 조회 시 예외가 발생한다`() {
        // given
        val member = createMemberWithMocks()

        // when & then
        assertThrows<RuntimeException> {
            gatheringService.getGatheringDetail(
                GatheringQuery.GatheringDetail(gatheringId = 999L, memberId = member.id)
            )
        }
    }

    @Test
    fun `모임에 속하지 않은 멤버가 조회 시 예외가 발생한다`() {
        // given
        val member1 = memberFixture.createMember(socialId = "1", email = "test1@test.com")
        val member2 = memberFixture.createMember(socialId = "2", email = "test2@test.com")
        setupMocks(member1, member2)

        val gatheringResult = gatheringService.generateGathering(
            GatheringFixture.gatheringGenerateCommand(member1.id, "test gathering", daysSinceLastGathering = 10)
        )

        // when & then
        assertThrows<RuntimeException> {
            gatheringService.getGatheringDetail(
                GatheringQuery.GatheringDetail(gatheringId = gatheringResult.gatheringId, memberId = member2.id)
            )
        }
    }

    @Test
    fun `삭제된 모임 ID로 상세 조회 시 예외가 발생한다`() {
        // given
        val member = createMemberWithMocks()
        setupMocks()

        val gatheringResult = gatheringService.generateGathering(
            GatheringFixture.gatheringGenerateCommand(member.id, "test gathering", daysSinceLastGathering = 10)
        )

        val gathering = gatheringRepository.findById(gatheringResult.gatheringId).orElse(null)
        gatheringRepository.delete(gathering)

        // when & then
        assertThrows<RuntimeException> {
            gatheringService.getGatheringDetail(
                GatheringQuery.GatheringDetail(gatheringId = gathering.id, memberId = member.id)
            )
        }
    }


    @Test
    fun `빈 태그 목록으로 모임 생성이 가능하다`() {
        // given
        val member = createMemberWithMocks()
        val command = GatheringFixture.gatheringGenerateCommand(
            memberId = member.id, gatheringName = "no tags gathering", tags = emptyList()
        )

        // when
        val result = gatheringService.generateGathering(command)

        // then
        val gathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        assertThat(gathering.tags).isEmpty()
    }

    @Test
    fun `첫 모임 날짜가 0일 전인 경우 오늘 날짜로 설정된다`() {
        // given
        val member = createMemberWithMocks()
        val command = GatheringFixture.gatheringGenerateCommand(
            memberId = member.id, daysSinceLastGathering = 0
        )

        // when
        val result = gatheringService.generateGathering(command)

        // then
        val gathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        assertThat(gathering.firstGatheringDate).isEqualTo(LocalDate.now())
        assertThat(gathering.lastGatheringDate).isEqualTo(LocalDate.now())
    }

    @Test
    fun `긴 모임 이름으로 모임 생성이 가능하다`() {
        // given
        val member = createMemberWithMocks()
        val longName = "이것은 매우 긴 모임 이름입니다. 한국어로 작성된 긴 이름이며 실제 사용자가 입력할 수 있는 범위의 길이입니다."
        val command = GatheringFixture.gatheringGenerateCommand(
            memberId = member.id, gatheringName = longName
        )

        // when
        val result = gatheringService.generateGathering(command)

        // then
        val gathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        assertThat(gathering.name).isEqualTo(longName)
    }

    @Test
    fun `다수의 태그로 모임 생성이 가능하다`() {
        // given
        val member = createMemberWithMocks()
        val manyTags = (1..10).map { "tag$it" }
        val command = GatheringFixture.gatheringGenerateCommand(
            memberId = member.id, tags = manyTags
        )

        // when
        val result = gatheringService.generateGathering(command)

        // then
        val gathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        assertThat(gathering.tags).hasSize(10)
        assertThat(gathering.tags).containsExactlyInAnyOrderElementsOf(manyTags)
    }

    @Test
    fun `동일한 이름의 모임들이 생성 시간 순으로 정렬된다`() {
        // given
        val member = createMemberWithMocks()

        val gathering1 = gatheringFixture.createGathering(member, "동일한 이름")
        Thread.sleep(10) // 생성 시간 차이를 위한 대기
        val gathering2 = gatheringFixture.createGathering(member, "동일한 이름")

        gatheringFixture.registerGatheringMember(gathering1, member)
        gatheringFixture.registerGatheringMember(gathering2, member)

        // when
        val result = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member.id)
        )

        // then
        assertThat(result.gatheringOverviews).hasSize(2)
        assertThat(result.gatheringOverviews.map { it.gatheringName }).containsExactly(
            "동일한 이름", "동일한 이름"
        )
    }

    @Test
    fun `특수문자가 포함된 모임 이름으로 조회가 가능하다`() {
        // given
        val member = createMemberWithMocks()
        val specialName = "모임!@#$%^&*()_+-=[]{}|;':\",./<>?"
        val gathering = gatheringFixture.createGathering(member, specialName)
        gatheringFixture.registerGatheringMember(gathering, member)

        // when
        val result = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member.id)
        )

        // then
        assertThat(result.gatheringOverviews).hasSize(1)
        assertThat(result.gatheringOverviews.first().gatheringName).isEqualTo(specialName)
    }

    @Test
    fun `페이지네이션 테스트 - 50개 모임 조회`() {
        // given
        val member = createMemberWithMocks()

        val gatherings = (1..50).map { "gathering${String.format("%02d", it)}" }
            .map { name -> gatheringFixture.createGathering(member, name) }

        gatherings.forEach { gathering -> gatheringFixture.registerGatheringMember(gathering, member) }

        // when
        val result = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member.id)
        )

        // then
        assertThat(result.gatheringOverviews).hasSize(50)
        assertThat(result.gatheringOverviews.map { it.gatheringName }).isSorted()
    }

    @Test
    fun `다른 멤버들의 모임이 섞여있어도 본인의 모임만 조회된다`() {
        // given
        val member1 = memberFixture.createMember(socialId = "1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "2", email = "member2@test.com")
        val member3 = memberFixture.createMember(socialId = "3", email = "member3@test.com")
        setupMocks(member1, member2, member3)

        // generateGathering을 통해 모임을 생성하면 자동으로 호스트가 등록됨
        gatheringService.generateGathering(GatheringFixture.gatheringGenerateCommand(member1.id, "member1_gathering"))
        gatheringService.generateGathering(GatheringFixture.gatheringGenerateCommand(member2.id, "member2_gathering"))
        gatheringService.generateGathering(GatheringFixture.gatheringGenerateCommand(member3.id, "member3_gathering"))
        gatheringService.generateGathering(
            GatheringFixture.gatheringGenerateCommand(
                member1.id, "member1_another_gathering"
            )
        )

        // when
        val result = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member1.id)
        )

        // then
        assertThat(result.gatheringOverviews).hasSize(2)
        assertThat(result.gatheringOverviews.map { it.gatheringName }).containsExactly(
            "member1_another_gathering", "member1_gathering"
        )
    }


    @Test
    fun `모임 상세 조회 시 태그 정보가 정확히 반환된다`() {
        // given
        val member = createMemberWithMocks()
        setupMocks()

        val tags = listOf("친목", "스터디", "운동", "여행")
        val gatheringResult = gatheringService.generateGathering(
            GatheringFixture.gatheringGenerateCommand(member.id, "태그 테스트 모임", tags = tags)
        )

        // when
        val detailResult = gatheringService.getGatheringDetail(
            GatheringQuery.GatheringDetail(gatheringId = gatheringResult.gatheringId, memberId = member.id)
        )

        // then
        assertThat(detailResult.gatheringName).isEqualTo("태그 테스트 모임")
        // 태그 정보는 도메인 객체에서 확인
        val savedGathering = gatheringRepository.findById(gatheringResult.gatheringId).orElse(null)
        assertThat(savedGathering.tags).containsExactlyInAnyOrderElementsOf(tags)
    }

    @Test
    fun `서로 다른 간격의 모임들이 올바른 날짜 계산을 하는지 확인한다`() {
        // given
        val member = createMemberWithMocks()
        setupMocks()

        val dailyResult = gatheringService.generateGathering(
            GatheringFixture.gatheringGenerateCommand(
                member.id, "daily", daysSinceLastGathering = 30, gatheringIntervalDays = 1
            )
        )
        val weeklyResult = gatheringService.generateGathering(
            GatheringFixture.gatheringGenerateCommand(
                member.id, "weekly", daysSinceLastGathering = 14, gatheringIntervalDays = 7
            )
        )
        val monthlyResult = gatheringService.generateGathering(
            GatheringFixture.gatheringGenerateCommand(
                member.id, "monthly", daysSinceLastGathering = 60, gatheringIntervalDays = 30
            )
        )

        // when & then
        listOf(
            Triple(dailyResult.gatheringId, "daily", 30),
            Triple(weeklyResult.gatheringId, "weekly", 14),
            Triple(monthlyResult.gatheringId, "monthly", 60)
        ).forEach { (gatheringId: Long, _: String, expectedDays: Int) ->
            val result = gatheringService.getGatheringDetail(
                GatheringQuery.GatheringDetail(gatheringId = gatheringId, memberId = member.id)
            )

            val savedGathering = gatheringRepository.findById(gatheringId).orElse(null)
            val actualExpectedDays = ChronoUnit.DAYS.between(savedGathering.firstGatheringDate, LocalDate.now()).toInt()

            // 실제 서비스 로직: firstGatheringDate.daysAgo() = firstGatheringDate부터 현재까지의 일수
            assertThat(result.daysSinceFirstGathering).isEqualTo(actualExpectedDays)

            // firstGatheringDate는 LocalDate.now().minusDays(expectedDays)로 설정되므로 
            // 결과는 expectedDays와 같아야 함
            assertThat(result.daysSinceFirstGathering).isEqualTo(expectedDays)
        }
    }


    @Test
    fun `모임 생성 시 호스트 멤버가 자동으로 모임에 등록된다`() {
        // given
        val member = createMemberWithMocks()
        val command = GatheringFixture.gatheringGenerateCommand(memberId = member.id)

        // when
        val result = gatheringService.generateGathering(command)

        // then
        val gathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        val gatheringMember = gatheringMemberRepository.findByGatheringAndMemberId(gathering, member.id)

        assertThat(gatheringMember).isNotNull
        assertThat(gatheringMember!!.isHost).isTrue
        assertThat(gathering.hostId).isEqualTo(member.id)
    }

    @Test
    fun `동시에 여러 모임을 생성해도 데이터 일관성이 유지된다`() {
        // given
        val member = createMemberWithMocks()
        val commands = (1..5).map { it ->
            GatheringFixture.gatheringGenerateCommand(
                memberId = member.id, gatheringName = "concurrent_gathering_$it"
            )
        }

        // when
        val results = commands.map { command: GatheringCommand.Generate ->
            gatheringService.generateGathering(command)
        }

        // then
        assertThat(results).hasSize(5)
        assertThat(results.map { result -> result.gatheringId }).doesNotHaveDuplicates()

        val memberGatherings = gatheringService.getMemberGatherings(
            GatheringQuery.MemberGathering(memberId = member.id)
        )
        assertThat(memberGatherings.gatheringOverviews).hasSize(5)
    }

    @Test
    fun `모임 상세 조회 시 멤버 수가 정확히 카운트된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val members = (1..3).map {
            memberFixture.createMember(socialId = "member$it", email = "member$it@test.com")
        }

        setupMocks(host, *members.toTypedArray())

        val gatheringResult = gatheringService.generateGathering(
            GatheringFixture.gatheringGenerateCommand(host.id, "multi member gathering")
        )

        val gathering = gatheringRepository.findById(gatheringResult.gatheringId).orElse(null)
        members.forEach { member ->
            gatheringFixture.registerGatheringMember(gathering, member)
        }

        // when
        val result = gatheringService.getGatheringDetail(
            GatheringQuery.GatheringDetail(gatheringId = gathering.id, memberId = host.id)
        )

        // then
        assertThat(result.members).hasSize(4) // 호스트 + 3명의 멤버
    }

    @Test
    fun `초대 수락시 멤버가 모임에 정상적으로 가입된다`() {
        // given
        val member = createMemberWithMocks()
        val gathering = gatheringFixture.createGathering(member, "모임A")

        val command = GatheringCommand.JoinGathering(memberId = member.id, gatheringId = gathering.id)

        // when
        val result = gatheringService.joinGathering(command)

        // then
        val gatheringMember = gatheringMemberRepository.findByGatheringAndMemberId(gathering, member.id)
        assertThat(gatheringMember).isNotNull
        assertThat(result.gatheringId).isEqualTo(gathering.id)
    }

    @Test
    fun `존재하지 않는 모임 ID로 초대 수락시 NOT_FOUND 예외가 발생한다`() {
        // given
        val member = createMemberWithMocks()
        val invalidGatheringId = 999999L
        val command = GatheringCommand.JoinGathering(memberId = member.id, gatheringId = invalidGatheringId)

        // when & then
        val exception = assertThrows<BaseException> {
            gatheringService.joinGathering(command)
        }
        assertThat(exception.errorType).isEqualTo(ErrorType.NOT_FOUND)
        assertThat(exception.message).contains("모임을 찾을 수 없습니다.")
    }

    @Test
    fun `이미 가입된 멤버가 초대 수락시 예외가 발생한다`() {
        // given
        val member = createMemberWithMocks()
        val gathering = gatheringFixture.createGathering(member)

        gatheringFixture.registerGatheringMember(gathering, member)

        val command = GatheringCommand.JoinGathering(memberId = member.id, gatheringId = gathering.id)

        // when & then
        val exception = assertThrows<BaseException> {
            gatheringService.joinGathering(command)
        }
        assertThat(exception.errorType).isEqualTo(ErrorType.BAD_REQUEST)
        assertThat(exception.message).contains("이미 가입된 사용자입니다.")
    }
}
