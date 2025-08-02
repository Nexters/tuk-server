package nexters.tuk.application.proposal

import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.vo.ProposalPurpose
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.domain.proposal.Proposal
import nexters.tuk.domain.proposal.ProposalRepository
import nexters.tuk.fixtures.GatheringFixtureHelper
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ProposalServiceIntegrationTest @Autowired constructor(
    private val proposalService: ProposalService,
    private val proposalRepository: ProposalRepository,
    private val memberRepository: MemberRepository,
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)
    private val gatheringFixture =
        GatheringFixtureHelper(gatheringRepository, gatheringMemberRepository)

    @AfterEach
    fun tearDown() {
        proposalRepository.deleteAllInBatch()
        gatheringMemberRepository.deleteAllInBatch()
        gatheringRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `제안을 성공적으로 발행한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(hostMember = host)
        val command = ProposalCommand.Propose(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = ProposalPurpose(
                where = "카페",
                time = "오후 3시",
                what = "커피 모임"
            )
        )

        // when
        val result = proposalService.propose(command)

        // then
        assertThat(result.proposalId).isNotNull()

        val savedProposal = proposalRepository.findById(result.proposalId).orElse(null)
        assertThat(savedProposal).isNotNull
        assertThat(savedProposal.proposerId).isEqualTo(host.id)
        assertThat(savedProposal.gatheringId).isEqualTo(gathering.id)
        assertThat(savedProposal.purpose).isEqualTo("카페\n오후 3시\n커피 모임")
    }

    @Test
    fun `모임의 제안 통계를 정확히 계산한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")
        val gathering = gatheringFixture.createGathering(hostMember = host)

        // host가 보낸 제안 2개
        proposalRepository.save(Proposal.publish(gathering.id, host.id, "첫번째 제안"))
        proposalRepository.save(Proposal.publish(gathering.id, host.id, "두번째 제안"))

        // member1이 보낸 제안 1개
        proposalRepository.save(Proposal.publish(gathering.id, member1.id, "멤버1 제안"))

        // member2가 보낸 제안 1개
        proposalRepository.save(Proposal.publish(gathering.id, member2.id, "멤버2 제안"))

        // when - host 관점에서 통계 조회
        val hostStat = proposalService.getGatheringProposalStat(gathering.id, host.id)

        // then
        assertThat(hostStat.sentCount).isEqualTo(2) // host가 보낸 제안 수
        assertThat(hostStat.receivedCount).isEqualTo(2) // host가 받은 제안 수 (member1 + member2)
    }

    @Test
    fun `제안이 없는 모임의 통계는 모두 0이다`() {
        // given
        val member = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(hostMember = member)

        // when
        val stat = proposalService.getGatheringProposalStat(gathering.id, member.id)

        // then
        assertThat(stat.sentCount).isEqualTo(0)
        assertThat(stat.receivedCount).isEqualTo(0)
    }

    @Test
    fun `다른 모임의 제안은 통계에 포함되지 않는다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering1 = gatheringFixture.createGathering(hostMember = host)
        val gathering2 = gatheringFixture.createGathering(hostMember = host)

        // gathering1에 제안들
        proposalRepository.save(Proposal.publish(gathering1.id, host.id, "gathering1 제안1"))
        proposalRepository.save(Proposal.publish(gathering1.id, member.id, "gathering1 제안2"))

        // gathering2에 제안들  
        proposalRepository.save(Proposal.publish(gathering2.id, host.id, "gathering2 제안1"))
        proposalRepository.save(Proposal.publish(gathering2.id, host.id, "gathering2 제안2"))
        proposalRepository.save(Proposal.publish(gathering2.id, member.id, "gathering2 제안3"))

        // when - gathering1에 대한 host의 통계
        val stat = proposalService.getGatheringProposalStat(gathering1.id, host.id)

        // then - gathering1의 제안만 카운트되어야 함
        assertThat(stat.sentCount).isEqualTo(1) // host가 gathering1에 보낸 제안
        assertThat(stat.receivedCount).isEqualTo(1) // member가 gathering1에 보낸 제안
    }

    @Test
    fun `혼자만 있는 모임에서는 보낸 제안만 카운트된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(hostMember = host)

        // host 혼자만 제안을 보냄
        proposalRepository.save(Proposal.publish(gathering.id, host.id, "혼자 제안1"))
        proposalRepository.save(Proposal.publish(gathering.id, host.id, "혼자 제안2"))

        // when
        val stat = proposalService.getGatheringProposalStat(gathering.id, host.id)

        // then
        assertThat(stat.sentCount).isEqualTo(2) // host가 보낸 제안
        assertThat(stat.receivedCount).isEqualTo(0) // 다른 사람이 보낸 제안 없음
    }

    @Test
    fun `복잡한 제안 상황의 통계를 정확히 계산한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")
        val member3 = memberFixture.createMember(socialId = "member3", email = "member3@test.com")
        val gathering = gatheringFixture.createGathering(hostMember = host)

        // 다양한 멤버들이 제안을 보냄
        // host: 3개
        proposalRepository.save(Proposal.publish(gathering.id, host.id, "host 제안1"))
        proposalRepository.save(Proposal.publish(gathering.id, host.id, "host 제안2"))
        proposalRepository.save(Proposal.publish(gathering.id, host.id, "host 제안3"))

        // member1: 2개
        proposalRepository.save(Proposal.publish(gathering.id, member1.id, "member1 제안1"))
        proposalRepository.save(Proposal.publish(gathering.id, member1.id, "member1 제안2"))

        // member2: 1개
        proposalRepository.save(Proposal.publish(gathering.id, member2.id, "member2 제안1"))

        // member3: 1개
        proposalRepository.save(Proposal.publish(gathering.id, member3.id, "member3 제안1"))

        // when - member1 관점에서 통계
        val member1Stat = proposalService.getGatheringProposalStat(gathering.id, member1.id)

        // then
        assertThat(member1Stat.sentCount).isEqualTo(2) // member1이 보낸 제안
        assertThat(member1Stat.receivedCount).isEqualTo(5) // 다른 멤버들이 보낸 제안 (host:3 + member2:1 + member3:1)
    }
}