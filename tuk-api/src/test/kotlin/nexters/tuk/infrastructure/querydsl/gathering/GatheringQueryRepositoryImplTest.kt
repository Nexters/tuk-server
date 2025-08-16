package nexters.tuk.infrastructure.querydsl.gathering

import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.proposal.Proposal
import nexters.tuk.domain.proposal.ProposalMember
import nexters.tuk.domain.proposal.ProposalMemberRepository
import nexters.tuk.domain.proposal.ProposalRepository
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.fixtures.GatheringFixtureHelper
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GatheringQueryRepositoryImplTest @Autowired constructor(
    private val gatheringQueryRepository: GatheringQueryRepositoryImpl,
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
    private val memberRepository: MemberRepository,
    private val proposalRepository: ProposalRepository,
    private val proposalMemberRepository: ProposalMemberRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)
    private val gatheringFixture = GatheringFixtureHelper(gatheringRepository, gatheringMemberRepository)

    @AfterEach
    fun tearDown() {
        proposalMemberRepository.deleteAllInBatch()
        proposalRepository.deleteAllInBatch()
        gatheringMemberRepository.deleteAllInBatch()
        gatheringRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `모임에서 사용자의 보낸받은 초대장 수를 정확히 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        
        // 모든 멤버를 모임에 추가
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        // host가 보낸 초대장 2개
        val proposal1 = createProposalWithMembers(host.id, gathering.id, "첫번째 제안", listOf(host.id, member1.id, member2.id))
        val proposal2 = createProposalWithMembers(host.id, gathering.id, "두번째 제안", listOf(host.id, member1.id, member2.id))

        // member1이 보낸 초대장 1개
        val proposal3 = createProposalWithMembers(member1.id, gathering.id, "역제안", listOf(host.id, member1.id, member2.id))

        // when - host의 통계 조회
        val hostStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering.id, host.id)

        // then - host는 2개 보냄, 1개 받음
        assertThat(hostStats.sentCount).isEqualTo(2)
        assertThat(hostStats.receivedCount).isEqualTo(1)

        // when - member1의 통계 조회
        val member1Stats = gatheringQueryRepository.findGatheringMemberProposalState(gathering.id, member1.id)

        // then - member1은 1개 보냄, 2개 받음
        assertThat(member1Stats.sentCount).isEqualTo(1)
        assertThat(member1Stats.receivedCount).isEqualTo(2)

        // when - member2의 통계 조회
        val member2Stats = gatheringQueryRepository.findGatheringMemberProposalState(gathering.id, member2.id)

        // then - member2는 0개 보냄, 3개 받음
        assertThat(member2Stats.sentCount).isEqualTo(0)
        assertThat(member2Stats.receivedCount).isEqualTo(3)
    }

    @Test
    fun `나중에 모임에 추가된 사용자의 통계가 정확히 계산된다`() {
        // given - 기존 멤버들과 초대장들이 있는 상황
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val existingMember = memberFixture.createMember(socialId = "existing", email = "existing@test.com")
        val newMember = memberFixture.createMember(socialId = "new", email = "new@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        
        // 기존 멤버들만 모임에 추가
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, existingMember.id))

        // 기존 멤버들 간의 초대장 생성
        val oldProposal1 = createProposalWithMembers(host.id, gathering.id, "기존 제안1", listOf(host.id, existingMember.id))
        val oldProposal2 = createProposalWithMembers(existingMember.id, gathering.id, "기존 제안2", listOf(host.id, existingMember.id))

        // when - 새로운 멤버를 모임에 추가
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, newMember.id))

        // 새로운 멤버가 포함된 초대장 생성
        val newProposal1 = createProposalWithMembers(host.id, gathering.id, "새 멤버 포함 제안1", listOf(host.id, existingMember.id, newMember.id))
        val newProposal2 = createProposalWithMembers(newMember.id, gathering.id, "새 멤버의 제안", listOf(host.id, existingMember.id, newMember.id))

        // then - 새로운 멤버의 통계는 자신이 참여한 초대장만 반영되어야 함
        val newMemberStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering.id, newMember.id)
        
        assertThat(newMemberStats.sentCount).isEqualTo(1) // 자신이 보낸 초대장 1개
        assertThat(newMemberStats.receivedCount).isEqualTo(1) // 자신이 받은 초대장 1개 (host가 보낸 것)
        
        // 기존 멤버들의 통계도 확인
        val hostStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering.id, host.id)
        assertThat(hostStats.sentCount).isEqualTo(2) // 기존 1개 + 새로운 1개
        assertThat(hostStats.receivedCount).isEqualTo(2) // existingMember가 보낸 1개 + newMember가 보낸 1개

        val existingMemberStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering.id, existingMember.id)
        assertThat(existingMemberStats.sentCount).isEqualTo(1) // 기존에 보낸 1개만
        assertThat(existingMemberStats.receivedCount).isEqualTo(3) // host가 보낸 2개 + newMember가 보낸 1개
    }

    @Test
    fun `초대장이 없는 모임에서는 모든 통계가 0이다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering = gatheringFixture.createGathering(host, "빈 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // when
        val hostStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering.id, host.id)
        val memberStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering.id, member.id)

        // then
        assertThat(hostStats.sentCount).isEqualTo(0)
        assertThat(hostStats.receivedCount).isEqualTo(0)
        assertThat(memberStats.sentCount).isEqualTo(0)
        assertThat(memberStats.receivedCount).isEqualTo(0)
    }

    @Test
    fun `혼자만 있는 모임에서는 자신이 보낸 초대장만 카운트된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")

        val gathering = gatheringFixture.createGathering(host, "혼자 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        // 혼자 보내는 초대장 (자기 자신에게만)
        val proposal = createProposalWithMembers(host.id, gathering.id, "혼자 제안", listOf(host.id))

        // when
        val hostStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering.id, host.id)

        // then - 자신이 보낸 것이므로 sent만 카운트
        assertThat(hostStats.sentCount).isEqualTo(1)
        assertThat(hostStats.receivedCount).isEqualTo(0)
    }

    @Test
    fun `다른 모임의 초대장은 통계에 포함되지 않는다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering1 = gatheringFixture.createGathering(host, "모임1")
        val gathering2 = gatheringFixture.createGathering(host, "모임2")
        
        // 각 모임에 멤버 추가
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, member.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, member.id))

        // 각 모임에서 초대장 생성
        val proposal1 = createProposalWithMembers(host.id, gathering1.id, "모임1 제안", listOf(host.id, member.id))
        val proposal2 = createProposalWithMembers(member.id, gathering2.id, "모임2 제안", listOf(host.id, member.id))

        // when - 모임1에서의 통계 조회
        val gathering1HostStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering1.id, host.id)
        val gathering1MemberStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering1.id, member.id)

        // then - 각 모임의 초대장만 카운트되어야 함
        assertThat(gathering1HostStats.sentCount).isEqualTo(1)
        assertThat(gathering1HostStats.receivedCount).isEqualTo(0)
        assertThat(gathering1MemberStats.sentCount).isEqualTo(0)
        assertThat(gathering1MemberStats.receivedCount).isEqualTo(1)

        // when - 모임2에서의 통계 조회
        val gathering2HostStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering2.id, host.id)
        val gathering2MemberStats = gatheringQueryRepository.findGatheringMemberProposalState(gathering2.id, member.id)

        // then
        assertThat(gathering2HostStats.sentCount).isEqualTo(0)
        assertThat(gathering2HostStats.receivedCount).isEqualTo(1)
        assertThat(gathering2MemberStats.sentCount).isEqualTo(1)
        assertThat(gathering2MemberStats.receivedCount).isEqualTo(0)
    }

    private fun createProposalWithMembers(
        proposerId: Long,
        gatheringId: Long,
        purpose: String,
        memberIds: List<Long>
    ): Proposal {
        val proposal = Proposal.publish(proposerId, purpose)
        proposal.registerGathering(gatheringId)
        proposalRepository.save(proposal)

        // 모든 멤버에게 ProposalMember 생성
        memberIds.forEach { memberId ->
            val proposalMember = ProposalMember.publish(proposal, memberId)
            proposalMemberRepository.save(proposalMember)
        }

        return proposal
    }
}