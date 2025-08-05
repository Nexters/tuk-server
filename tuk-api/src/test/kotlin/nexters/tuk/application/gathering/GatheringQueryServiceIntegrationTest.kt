package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringQuery
import nexters.tuk.contract.BaseException
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.proposal.Proposal
import nexters.tuk.domain.proposal.ProposalRepository
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
class GatheringQueryServiceIntegrationTest @Autowired constructor(
    private val gatheringQueryService: GatheringQueryService,
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
    private val memberRepository: MemberRepository,
    private val proposalRepository: ProposalRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)
    private val gatheringFixture = GatheringFixtureHelper(gatheringRepository, gatheringMemberRepository)

    @AfterEach
    fun tearDown() {
        proposalRepository.deleteAllInBatch()
        gatheringMemberRepository.deleteAllInBatch()
        gatheringRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `멤버가 속한 모임 목록을 정상적으로 조회한다`() {
        // given
        val member = memberFixture.createMember()
        val host1 = memberFixture.createMember(socialId = "host1", email = "host1@test.com")
        val host2 = memberFixture.createMember(socialId = "host2", email = "host2@test.com")

        val gathering1 = gatheringFixture.createGathering(host1, "모임1")
        val gathering2 = gatheringFixture.createGathering(host2, "모임2")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, member.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, member.id))

        val query = GatheringQuery.MemberGathering(member.id)

        // when
        val result = gatheringQueryService.getMemberGatherings(query)

        // then
        assertThat(result.totalCount).isEqualTo(2)
        assertThat(result.gatheringOverviews).hasSize(2)
        
        val gatheringNames = result.gatheringOverviews.map { it.gatheringName }
        assertThat(gatheringNames).containsExactlyInAnyOrder("모임1", "모임2")
        
        // 모든 relativeTime은 현재 0일로 설정되어 "오늘"
        result.gatheringOverviews.forEach {
            assertThat(it.lastNotificationRelativeTime.value).isEqualTo("오늘")
        }
    }

    @Test
    fun `모임에 속하지 않은 멤버는 빈 목록을 조회한다`() {
        // given
        val member = memberFixture.createMember()
        val query = GatheringQuery.MemberGathering(member.id)

        // when
        val result = gatheringQueryService.getMemberGatherings(query)

        // then
        assertThat(result.totalCount).isEqualTo(0)
        assertThat(result.gatheringOverviews).isEmpty()
    }

    @Test
    fun `단일 모임에만 속한 멤버의 목록을 조회한다`() {
        // given
        val member = memberFixture.createMember()
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")

        val gathering = gatheringFixture.createGathering(host, "단일 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val query = GatheringQuery.MemberGathering(member.id)

        // when
        val result = gatheringQueryService.getMemberGatherings(query)

        // then
        assertThat(result.totalCount).isEqualTo(1)
        assertThat(result.gatheringOverviews).hasSize(1)
        assertThat(result.gatheringOverviews.first().gatheringName).isEqualTo("단일 모임")
        assertThat(result.gatheringOverviews.first().gatheringId).isEqualTo(gathering.id)
    }

    @Test
    fun `모임 상세 정보를 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        
        // 멤버들을 모임에 추가
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        // 제안 생성 (보낸 제안 2개, 받은 제안 1개)
        proposalRepository.save(Proposal.publish(gathering, host.id, "모임 제안"))
        proposalRepository.save(Proposal.publish(gathering, host.id, "모임 제안"))
        proposalRepository.save(Proposal.publish(gathering, member1.id, "모임 제안"))

        val query = GatheringQuery.GatheringDetail(host.id, gathering.id)

        // when
        val result = gatheringQueryService.getGatheringDetail(query)

        // then
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringName).isEqualTo("테스트 모임")
        assertThat(result.lastNotificationRelativeTime.value).isEqualTo("오늘") // 현재는 하드코딩
        assertThat(result.sentProposalCount).isEqualTo(2)
        assertThat(result.receivedProposalCount).isEqualTo(1)
        assertThat(result.members).hasSize(3)
        
        val memberNames = result.members.map { it.memberName }
        assertThat(memberNames).containsExactlyInAnyOrder("테스트사용자", "테스트사용자", "테스트사용자")
    }

    @Test
    fun `모임에 접근 권한이 없는 멤버는 상세 정보를 조회할 수 없다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val nonMember = memberFixture.createMember(socialId = "nonMember", email = "nonMember@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val query = GatheringQuery.GatheringDetail(nonMember.id, gathering.id)

        // when & then
        assertThrows<BaseException> {
            gatheringQueryService.getGatheringDetail(query)
        }
    }

    @Test
    fun `존재하지 않는 모임의 상세 정보를 조회하면 예외가 발생한다`() {
        // given
        val member = memberFixture.createMember()
        val nonExistentGatheringId = 999999L

        val query = GatheringQuery.GatheringDetail(member.id, nonExistentGatheringId)

        // when & then
        assertThrows<BaseException> {
            gatheringQueryService.getGatheringDetail(query)
        }
    }

    @Test
    fun `제안이 없는 모임의 상세 정보를 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering = gatheringFixture.createGathering(host, "제안 없는 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val query = GatheringQuery.GatheringDetail(host.id, gathering.id)

        // when
        val result = gatheringQueryService.getGatheringDetail(query)

        // then
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringName).isEqualTo("제안 없는 모임")
        assertThat(result.sentProposalCount).isEqualTo(0)
        assertThat(result.receivedProposalCount).isEqualTo(0)
        assertThat(result.members).hasSize(2)
    }

    @Test
    fun `혼자만 있는 모임의 상세 정보를 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")

        val gathering = gatheringFixture.createGathering(host, "혼자 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val query = GatheringQuery.GatheringDetail(host.id, gathering.id)

        // when
        val result = gatheringQueryService.getGatheringDetail(query)

        // then
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringName).isEqualTo("혼자 모임")
        assertThat(result.sentProposalCount).isEqualTo(0)
        assertThat(result.receivedProposalCount).isEqualTo(0)
        assertThat(result.members).hasSize(1)
        assertThat(result.members.first().memberName).isEqualTo("테스트사용자")
        assertThat(result.members.first().memberId).isEqualTo(host.id)
    }

    @Test
    fun `다양한 상태의 제안이 있는 모임의 상세 정보를 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "다양한 제안 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))

        // 다양한 상태의 제안 생성
        proposalRepository.save(Proposal.publish(gathering, host.id, "첫번째 제안"))
        proposalRepository.save(Proposal.publish(gathering, host.id, "두번째 제안"))
        proposalRepository.save(Proposal.publish(gathering, member1.id, "역제안"))

        val query = GatheringQuery.GatheringDetail(host.id, gathering.id)

        // when
        val result = gatheringQueryService.getGatheringDetail(query)

        // then
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.sentProposalCount).isEqualTo(2) // 상태와 관계없이 보낸 제안 수
        assertThat(result.receivedProposalCount).isEqualTo(1)
        assertThat(result.members).hasSize(2)
    }

    @Test
    fun `많은 멤버가 있는 모임의 목록을 조회한다`() {
        // given
        val member = memberFixture.createMember()
        val gatherings = (1..10).map { index ->
            val host = memberFixture.createMember(socialId = "host$index", email = "host$index@test.com")
            val gathering = gatheringFixture.createGathering(host, "모임$index")
            gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))
            gathering
        }

        val query = GatheringQuery.MemberGathering(member.id)

        // when
        val result = gatheringQueryService.getMemberGatherings(query)

        // then
        assertThat(result.totalCount).isEqualTo(10)
        assertThat(result.gatheringOverviews).hasSize(10)
        
        val expectedNames = (1..10).map { "모임$it" }
        val actualNames = result.gatheringOverviews.map { it.gatheringName }
        assertThat(actualNames).containsExactlyInAnyOrderElementsOf(expectedNames)
    }
}