package nexters.tuk.application.proposal

import nexters.tuk.contract.BaseException
import nexters.tuk.domain.proposal.Proposal
import nexters.tuk.domain.proposal.ProposalMemberRepository
import nexters.tuk.domain.proposal.ProposalRepository
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ProposalMemberServiceIntegrationTest @Autowired constructor(
    private val proposalMemberService: ProposalMemberService,
    private val proposalRepository: ProposalRepository,
    private val proposalMemberRepository: ProposalMemberRepository,
    private val memberRepository: MemberRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)

    @AfterEach
    fun tearDown() {
        proposalMemberRepository.deleteAllInBatch()
        proposalRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `제안에 멤버들을 성공적으로 발행한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")
        val member3 = memberFixture.createMember(socialId = "member3", email = "member3@test.com")

        val proposal = Proposal.publish(
            proposerId = host.id,
            purpose = "테스트 제안"
        )
        proposal.registerGathering(1L)
        proposalRepository.save(proposal)

        val memberIds = listOf(member1.id, member2.id, member3.id)

        // when
        val result = proposalMemberService.publishGatheringMembers(proposal.id, memberIds)

        // then
        assertThat(result.proposalMemberIds).hasSize(3)
        result.proposalMemberIds.forEach { proposalMemberId ->
            assertThat(proposalMemberId).isNotNull()
        }

        // 실제로 저장되었는지 확인
        val savedProposalMembers = proposalMemberRepository.findAll()
        assertThat(savedProposalMembers).hasSize(3)

        val savedMemberIds = savedProposalMembers.map { it.memberId }
        assertThat(savedMemberIds).containsExactlyInAnyOrder(member1.id, member2.id, member3.id)

        // 제안과 올바르게 연결되었는지 확인
        savedProposalMembers.forEach { proposalMember ->
            assertThat(proposalMember.proposal.id).isEqualTo(proposal.id)
            assertThat(proposalMember.isRead).isFalse()
        }
    }

    @Test
    fun `단일 멤버에게 제안을 발행할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val proposal = Proposal.publish(
            proposerId = host.id,
            purpose = "단일 멤버 제안"
        )
        proposal.registerGathering(1L)
        proposalRepository.save(proposal)

        val memberIds = listOf(member.id)

        // when
        val result = proposalMemberService.publishGatheringMembers(proposal.id, memberIds)

        // then
        assertThat(result.proposalMemberIds).hasSize(1)
        
        val savedProposalMembers = proposalMemberRepository.findAll()
        assertThat(savedProposalMembers).hasSize(1)
        assertThat(savedProposalMembers.first().memberId).isEqualTo(member.id)
        assertThat(savedProposalMembers.first().proposal.id).isEqualTo(proposal.id)
    }

    @Test
    fun `빈 멤버 목록으로 제안을 발행하면 아무것도 생성되지 않는다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")

        val proposal = Proposal.publish(
            proposerId = host.id,
            purpose = "빈 멤버 목록 테스트"
        )
        proposal.registerGathering(1L)
        proposalRepository.save(proposal)

        val emptyMemberIds = emptyList<Long>()

        // when
        val result = proposalMemberService.publishGatheringMembers(proposal.id, emptyMemberIds)

        // then
        assertThat(result.proposalMemberIds).isEmpty()
        
        val savedProposalMembers = proposalMemberRepository.findAll()
        assertThat(savedProposalMembers).isEmpty()
    }

    @Test
    fun `존재하지 않는 제안 ID로 멤버를 발행하면 예외가 발생한다`() {
        // given
        val member = memberFixture.createMember()
        val nonExistentProposalId = 999999L
        val memberIds = listOf(member.id)

        // when & then
        val exception = assertThrows<BaseException> {
            proposalMemberService.publishGatheringMembers(nonExistentProposalId, memberIds)
        }

        assertThat(exception.message).isEqualTo("찾을 수 없는 제안입니다.")
    }

    @Test
    fun `중복된 멤버 ID가 있어도 모두 발행된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val proposal = Proposal.publish(
            proposerId = host.id,
            purpose = "중복 멤버 테스트"
        )
        proposal.registerGathering(1L)
        proposalRepository.save(proposal)

        val memberIds = listOf(member1.id, member2.id, member1.id) // member1 중복

        // when
        val result = proposalMemberService.publishGatheringMembers(proposal.id, memberIds)

        // then
        assertThat(result.proposalMemberIds).hasSize(3) // 중복되어도 3개 생성
        
        val savedProposalMembers = proposalMemberRepository.findAll()
        assertThat(savedProposalMembers).hasSize(3)
        
        val savedMemberIds = savedProposalMembers.map { it.memberId }
        assertThat(savedMemberIds).containsExactly(member1.id, member2.id, member1.id)
    }

    @Test
    fun `여러 제안에 대해 독립적으로 멤버를 발행할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val proposal1 = Proposal.publish(
            proposerId = host.id,
            purpose = "첫번째 제안"
        )
        proposal1.registerGathering(1L)
        proposalRepository.save(proposal1)

        val proposal2 = Proposal.publish(
            proposerId = host.id,
            purpose = "두번째 제안"
        )
        proposal2.registerGathering(2L)
        proposalRepository.save(proposal2)

        // when
        val result1 = proposalMemberService.publishGatheringMembers(proposal1.id, listOf(member1.id))
        val result2 = proposalMemberService.publishGatheringMembers(proposal2.id, listOf(member2.id))

        // then
        assertThat(result1.proposalMemberIds).hasSize(1)
        assertThat(result2.proposalMemberIds).hasSize(1)
        
        val savedProposalMembers = proposalMemberRepository.findAll()
        assertThat(savedProposalMembers).hasSize(2)

        // 각 제안별로 멤버 수 확인
        val proposal1Members = savedProposalMembers.filter { it.proposal.id == proposal1.id }
        val proposal2Members = savedProposalMembers.filter { it.proposal.id == proposal2.id }
        
        assertThat(proposal1Members).hasSize(1)
        assertThat(proposal1Members.first().memberId).isEqualTo(member1.id)
        
        assertThat(proposal2Members).hasSize(1)
        assertThat(proposal2Members.first().memberId).isEqualTo(member2.id)
    }
}