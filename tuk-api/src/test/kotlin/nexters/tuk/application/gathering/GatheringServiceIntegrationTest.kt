package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.contract.BaseException
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.fixtures.GatheringFixtureHelper
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GatheringServiceIntegrationTest @Autowired constructor(
    private val gatheringQueryService: GatheringQueryService,
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
    private val memberRepository: MemberRepository,
) {

    @Autowired
    private lateinit var gatheringService: GatheringService

    private val memberFixture = MemberFixtureHelper(memberRepository)
    private val gatheringFixture =
        GatheringFixtureHelper(gatheringRepository, gatheringMemberRepository)

    @AfterEach
    fun tearDown() {
        gatheringMemberRepository.deleteAllInBatch()
        gatheringRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `모임 이름을 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(host, "테스트 모임")

        // when
        val result = gatheringQueryService.getGatheringName(gathering.id)

        // then
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringName).isEqualTo("테스트 모임")
    }

    @Test
    fun `다양한 모임 이름을 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gatheringNames = listOf(
            "스터디 모임",
            "취미 모임",
            "운동 모임",
            "독서 모임",
            "코딩 테스트 스터디"
        )

        val gatherings = gatheringNames.map { name ->
            gatheringFixture.createGathering(host, name)
        }

        // when & then
        gatherings.forEachIndexed { index, gathering ->
            val result = gatheringQueryService.getGatheringName(gathering.id)
            assertThat(result.gatheringId).isEqualTo(gathering.id)
            assertThat(result.gatheringName).isEqualTo(gatheringNames[index])
        }
    }

    @Test
    fun `존재하지 않는 모임 ID로 조회하면 예외가 발생한다`() {
        // given
        val nonExistentGatheringId = 999999L

        // when & then
        val exception = assertThrows<BaseException> {
            gatheringQueryService.getGatheringName(nonExistentGatheringId)
        }

        assertThat(exception.message).isEqualTo("찾을 수 없는 모임입니다.")
    }

    @Test
    fun `삭제된 모임은 조회할 수 없다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(host, "삭제될 모임")

        // 모임을 soft delete
        gathering.delete()
        gatheringRepository.save(gathering)

        // when & then
        val exception = assertThrows<BaseException> {
            gatheringQueryService.getGatheringName(gathering.id)
        }

        assertThat(exception.message).isEqualTo("찾을 수 없는 모임입니다.")
    }

    @Test
    fun `특수 문자가 포함된 모임 이름도 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val specialName = "🎉 신년회 모임 2024! (매주 토요일) 🎊"
        val gathering = gatheringFixture.createGathering(host, specialName)

        // when
        val result = gatheringQueryService.getGatheringName(gathering.id)

        // then
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringName).isEqualTo(specialName)
    }

    @Test
    fun `빈 문자열 모임 이름도 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val emptyName = ""
        val gathering = gatheringFixture.createGathering(host, emptyName)

        // when
        val result = gatheringQueryService.getGatheringName(gathering.id)

        // then
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringName).isEqualTo(emptyName)
    }

    @Test
    fun `긴 모임 이름도 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val longName = "a".repeat(255) // 255자 긴 이름
        val gathering = gatheringFixture.createGathering(host, longName)

        // when
        val result = gatheringQueryService.getGatheringName(gathering.id)

        // then
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringName).isEqualTo(longName)
        assertThat(result.gatheringName).hasSize(255)
    }

    @Test
    fun `여러 번 조회해도 동일한 결과를 반환한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(host, "일관성 테스트 모임")

        // when
        val result1 = gatheringQueryService.getGatheringName(gathering.id)
        val result2 = gatheringQueryService.getGatheringName(gathering.id)
        val result3 = gatheringQueryService.getGatheringName(gathering.id)

        // then
        assertThat(result1.gatheringId).isEqualTo(result2.gatheringId)
            .isEqualTo(result3.gatheringId)
        assertThat(result1.gatheringName).isEqualTo(result2.gatheringName)
            .isEqualTo(result3.gatheringName)
        assertThat(result1.gatheringName).isEqualTo("일관성 테스트 모임")
    }

    @Test
    fun `동시에 여러 모임을 조회할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering1 = gatheringFixture.createGathering(host, "첫 번째 모임")
        val gathering2 = gatheringFixture.createGathering(host, "두 번째 모임")
        val gathering3 = gatheringFixture.createGathering(host, "세 번째 모임")

        // when
        val result1 = gatheringQueryService.getGatheringName(gathering1.id)
        val result2 = gatheringQueryService.getGatheringName(gathering2.id)
        val result3 = gatheringQueryService.getGatheringName(gathering3.id)

        // then
        assertThat(result1.gatheringId).isEqualTo(gathering1.id)
        assertThat(result1.gatheringName).isEqualTo("첫 번째 모임")

        assertThat(result2.gatheringId).isEqualTo(gathering2.id)
        assertThat(result2.gatheringName).isEqualTo("두 번째 모임")

        assertThat(result3.gatheringId).isEqualTo(gathering3.id)
        assertThat(result3.gatheringName).isEqualTo("세 번째 모임")
    }

    @Test
    fun `모임 주기를 변경 가능하다`() {
        // given
        val gathering = gatheringRepository.save(
            Gathering.generate(
                hostId = 1L,
                name = "모임",
                intervalDays = 10
            )
        )

        // when
        val actual = gatheringService.updateGathering(
            GatheringCommand.Update(
                gatheringId = gathering.id,
                gatheringIntervalDays = 30
            )
        )

        // then
        assertAll(
            { assertThat(actual).isNotNull },
            {
                assertThat(gatheringRepository.findByIdOrThrow(gatheringId = gathering.id).intervalDays)
                    .isEqualTo(30)
            },
        )
    }
}