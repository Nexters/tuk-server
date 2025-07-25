package nexters.tuk.application.gathering

import nexters.tuk.contract.BaseException
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.fixtures.GatheringFixtureHelper
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GatheringMemberServiceIntegrationTest @Autowired constructor(
    private val gatheringMemberService: GatheringMemberService,
    private val gatheringRepository: GatheringRepository,
    private val memberRepository: MemberRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)
    private val gatheringFixture = GatheringFixtureHelper(gatheringRepository, gatheringMemberRepository)


    @AfterEach
    fun tearDown() {
        gatheringMemberRepository.deleteAllInBatch()
        gatheringRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `모임의 멤버 목록을 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "test gathering")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        // when
        val result = gatheringMemberService.getGatheringMemberIds(gathering.id)

        // then
        assertThat(result).hasSize(3)
        assertThat(result).containsExactlyInAnyOrder(host.id, member1.id, member2.id)
    }

    @Test
    fun `멤버가 속한 모임 목록을 정상적으로 조회한다`() {
        // given
        val member = memberFixture.createMember()
        val host1 = memberFixture.createMember(socialId = "host1", email = "host1@test.com")
        val host2 = memberFixture.createMember(socialId = "host2", email = "host2@test.com")

        val gathering1 = gatheringFixture.createGathering(host1, "gathering1")
        val gathering2 = gatheringFixture.createGathering(host2, "gathering2")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, member.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, member.id))

        // when
        val result = gatheringMemberService.getMemberGatherings(member.id)

        // then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.name }).containsExactlyInAnyOrder("gathering1", "gathering2")
    }

    @Test
    fun `호스트 멤버를 정상적으로 초기화한다`() {
        // given
        val host = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(host)

        // when
        val result = gatheringMemberService.registerMember(gathering.id, host.id)

        // then
        assertThat(result).isNotNull()

        // DB에 저장되었는지 확인
        val saved = gatheringMemberRepository.findByGatheringAndMemberId(gathering, host.id)
        assertThat(saved).isNotNull
        assertThat(saved!!.isHost).isTrue
    }

    @Test
    fun `모임 접근 권한이 있는 멤버는 검증을 통과한다`() {
        // given
        val member = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(member)
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // when & then - 예외가 발생하지 않아야 함
        gatheringMemberService.verifyGatheringAccess(gathering.id, member.id)
    }

    @Test
    fun `모임 접근 권한이 없는 멤버는 예외가 발생한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val nonMember = memberFixture.createMember(socialId = "nonMember", email = "nonMember@test.com")

        val gathering = gatheringFixture.createGathering(host)
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        // when & then
        assertThrows<BaseException> {
            gatheringMemberService.verifyGatheringAccess(gathering.id, nonMember.id)
        }
    }

    @Test
    fun `빈 모임에서 멤버 목록을 조회하면 빈 리스트를 반환한다`() {
        // given
        val host = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(host)

        // when
        val result = gatheringMemberService.getGatheringMemberIds(gathering.id)

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `모임에 속하지 않은 멤버의 모임 목록을 조회하면 빈 리스트를 반환한다`() {
        // given
        val member = memberFixture.createMember()

        // when
        val result = gatheringMemberService.getMemberGatherings(member.id)

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `같은 멤버를 여러 모임에 등록할 수 있다`() {
        // given
        val member = memberFixture.createMember()
        val host1 = memberFixture.createMember(socialId = "host1", email = "host1@test.com")
        val host2 = memberFixture.createMember(socialId = "host2", email = "host2@test.com")
        val host3 = memberFixture.createMember(socialId = "host3", email = "host3@test.com")

        val gathering1 = gatheringFixture.createGathering(host1, "gathering1")
        val gathering2 = gatheringFixture.createGathering(host2, "gathering2")
        val gathering3 = gatheringFixture.createGathering(host3, "gathering3")

        // when
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, member.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, member.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering3, member.id))

        val result = gatheringMemberService.getMemberGatherings(member.id)

        // then
        assertThat(result).hasSize(3)
        assertThat(result.map { it.name }).containsExactlyInAnyOrder("gathering1", "gathering2", "gathering3")
    }

    @Test
    fun `호스트 초기화 시 기존 관계가 있어도 중복 생성하지 않는다`() {
        // given
        val host = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(host)

        // 먼저 호스트 관계를 생성
        gatheringMemberService.registerMember(gathering.id, host.id)

        // when - 같은 호스트로 다시 등록 시도 (예외 발생해야 함)
        assertThrows<BaseException> {
            gatheringMemberService.registerMember(gathering.id, host.id)
        }

        // then
        // DB에 중복으로 저장되지 않았는지 확인
        val allGatheringMembers = gatheringMemberRepository.findAllByGathering(gathering)
        assertThat(allGatheringMembers).hasSize(1) // 중복 생성이 방지되어 1개만 있어야 함
    }
}