package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringProposalCommand
import nexters.tuk.contract.BaseException
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.domain.proposal.Proposal
import nexters.tuk.domain.proposal.ProposalMemberRepository
import nexters.tuk.domain.proposal.ProposalRepository
import nexters.tuk.fixtures.GatheringFixtureHelper
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GatheringProposalServiceIntegrationTest @Autowired constructor(
    private val gatheringProposalService: GatheringProposalService,
    private val proposalRepository: ProposalRepository,
    private val proposalMemberRepository: ProposalMemberRepository,
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
    private val memberRepository: MemberRepository,
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
    fun `모임에 초대장을 성공적으로 추가한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        
        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        // 초대장 미리 생성 (gatheringId 없이)
        val proposal = proposalRepository.save(Proposal.publish(host.id, "카페에서 커피 마시기"))

        val command = GatheringProposalCommand.AddProposal(
            memberId = host.id,
            gatheringId = gathering.id,
            proposalId = proposal.id
        )

        // when
        gatheringProposalService.addProposal(command)

        // then
        // 초대장에 모임이 등록되었는지 확인
        val updatedProposal = proposalRepository.findById(proposal.id).orElse(null)
        assertThat(updatedProposal).isNotNull
        assertThat(updatedProposal.gatheringId).isEqualTo(gathering.id)

        // 모든 모임 멤버에게 초대장이 발행되었는지 확인
        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).hasSize(3)

        val proposalMemberIds = proposalMembers.map { it.memberId }
        assertThat(proposalMemberIds).containsExactlyInAnyOrder(host.id, member1.id, member2.id)

        // 모든 제안 멤버가 같은 제안에 연결되었는지 확인
        proposalMembers.forEach { proposalMember ->
            assertThat(proposalMember.proposal.id).isEqualTo(proposal.id)
            assertThat(proposalMember.isRead).isFalse()
        }
    }

    @Test
    fun `모임에 접근 권한이 없는 멤버가 초대장을 추가하면 예외가 발생한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val nonMember = memberFixture.createMember(socialId = "nonMember", email = "nonMember@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val proposal = proposalRepository.save(Proposal.publish(nonMember.id, "무단 침입 제안"))

        val command = GatheringProposalCommand.AddProposal(
            memberId = nonMember.id, // 모임에 속하지 않은 멤버
            gatheringId = gathering.id,
            proposalId = proposal.id
        )

        // when & then
        assertThrows<BaseException> {
            gatheringProposalService.addProposal(command)
        }

        // 초대장이 모임에 연결되지 않았는지 확인
        val updatedProposal = proposalRepository.findById(proposal.id).orElse(null)
        assertThat(updatedProposal.gatheringId).isNull()

        // 제안 멤버가 생성되지 않았는지 확인
        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).isEmpty()
    }

    @Test
    fun `존재하지 않는 초대장 ID로 추가하면 예외가 발생한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val nonExistentProposalId = 999999L

        val command = GatheringProposalCommand.AddProposal(
            memberId = host.id,
            gatheringId = gathering.id,
            proposalId = nonExistentProposalId
        )

        // when & then
        assertThrows<BaseException> {
            gatheringProposalService.addProposal(command)
        }

        // 제안 멤버가 생성되지 않았는지 확인
        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).isEmpty()
    }

    @Test
    fun `존재하지 않는 모임 ID로 추가하면 예외가 발생한다`() {
        // given
        val member = memberFixture.createMember()
        val proposal = proposalRepository.save(Proposal.publish(member.id, "고아 제안"))
        val nonExistentGatheringId = 999999L

        val command = GatheringProposalCommand.AddProposal(
            memberId = member.id,
            gatheringId = nonExistentGatheringId,
            proposalId = proposal.id
        )

        // when & then
        assertThrows<BaseException> {
            gatheringProposalService.addProposal(command)
        }

        // 초대장이 모임에 연결되지 않았는지 확인
        val updatedProposal = proposalRepository.findById(proposal.id).orElse(null)
        assertThat(updatedProposal.gatheringId).isNull()
    }

    @Test
    fun `혼자만 있는 모임에서도 초대장을 추가할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(host, "혼자 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val proposal = proposalRepository.save(Proposal.publish(host.id, "혼자 하는 일"))

        val command = GatheringProposalCommand.AddProposal(
            memberId = host.id,
            gatheringId = gathering.id,
            proposalId = proposal.id
        )

        // when
        gatheringProposalService.addProposal(command)

        // then
        val updatedProposal = proposalRepository.findById(proposal.id).orElse(null)
        assertThat(updatedProposal.gatheringId).isEqualTo(gathering.id)

        // 호스트 한 명에게만 제안이 발행되었는지 확인
        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).hasSize(1)
        assertThat(proposalMembers.first().memberId).isEqualTo(host.id)
    }

    @Test
    fun `이미 모임이 등록된 초대장에 다시 등록하면 예외가 발생한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering1 = gatheringFixture.createGathering(host, "첫 번째 모임")
        val gathering2 = gatheringFixture.createGathering(host, "두 번째 모임")
        
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, host.id))

        // 초대장 생성 후 첫 번째 모임에 등록
        val proposal = proposalRepository.save(Proposal.publish(host.id, "이중 등록 시도"))
        proposal.registerGathering(gathering1.id)
        proposalRepository.save(proposal)

        val command = GatheringProposalCommand.AddProposal(
            memberId = host.id,
            gatheringId = gathering2.id, // 다른 모임에 등록 시도
            proposalId = proposal.id
        )

        // when & then
        // 이미 모임이 등록된 초대장은 다른 모임에 등록할 수 없음
        assertThrows<IllegalArgumentException> {
            gatheringProposalService.addProposal(command)
        }

        // 초대장은 여전히 첫 번째 모임에만 연결되어 있음
        val updatedProposal = proposalRepository.findById(proposal.id).orElse(null)
        assertThat(updatedProposal.gatheringId).isEqualTo(gathering1.id)
    }

    @Test
    fun `대규모 모임에서도 모든 멤버에게 초대장이 발행된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(host, "대규모 모임")

        // 호스트 포함 20명의 멤버 생성
        val members = mutableListOf(host)
        for (i in 1..19) {
            val member = memberFixture.createMember(socialId = "member$i", email = "member$i@test.com")
            members.add(member)
            gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))
        }
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val proposal = proposalRepository.save(Proposal.publish(host.id, "대규모 모임 제안"))

        val command = GatheringProposalCommand.AddProposal(
            memberId = host.id,
            gatheringId = gathering.id,
            proposalId = proposal.id
        )

        // when
        gatheringProposalService.addProposal(command)

        // then
        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).hasSize(20) // 호스트 포함 20명

        val proposalMemberIds = proposalMembers.map { it.memberId }
        val expectedMemberIds = members.map { it.id }
        assertThat(proposalMemberIds).containsExactlyInAnyOrderElementsOf(expectedMemberIds)
    }
}