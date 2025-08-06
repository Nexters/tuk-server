package nexters.tuk.application.proposal

import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.vo.ProposalPurposeInfo
import nexters.tuk.contract.BaseException
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.proposal.ProposalMemberRepository
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
class ProposalCreateServiceIntegrationTest @Autowired constructor(
    private val proposalCreateService: ProposalCreateService,
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
    fun `제안을 성공적으로 발행한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        
        // 호스트와 멤버들을 모임에 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        val command = ProposalCommand.Propose(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = ProposalPurposeInfo(
                whereTag = "카페",
                whenTag = "오후 3시",
                whatTag = "커피 모임"
            )
        )

        // when
        val result = proposalCreateService.propose(command)

        // then
        assertThat(result.proposalId).isNotNull()

        // 제안이 생성되었는지 확인
        val savedProposal = proposalRepository.findById(result.proposalId).orElse(null)
        assertThat(savedProposal).isNotNull
        assertThat(savedProposal.proposerId).isEqualTo(host.id)
        assertThat(savedProposal.gatheringId).isEqualTo(gathering.id)
        assertThat(savedProposal.purpose).isEqualTo("카페\n오후 3시\n커피 모임")

        // 모든 모임 멤버에게 제안이 발행되었는지 확인 (총 3명)
        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).hasSize(3)

        val proposalMemberIds = proposalMembers.map { it.memberId }
        assertThat(proposalMemberIds).containsExactlyInAnyOrder(host.id, member1.id, member2.id)

        // 모든 제안 멤버가 같은 제안에 연결되었는지 확인
        proposalMembers.forEach { proposalMember ->
            assertThat(proposalMember.proposal.id).isEqualTo(result.proposalId)
            assertThat(proposalMember.isRead).isFalse()
        }
    }

    @Test
    fun `혼자만 있는 모임에서도 제안을 발행할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(host, "혼자 모임")
        
        // 호스트만 모임에 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val command = ProposalCommand.Propose(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = ProposalPurposeInfo(
                whereTag = "집",
                whenTag = "저녁 7시",
                whatTag = "혼자 공부"
            )
        )

        // when
        val result = proposalCreateService.propose(command)

        // then
        assertThat(result.proposalId).isNotNull()

        val savedProposal = proposalRepository.findById(result.proposalId).orElse(null)
        assertThat(savedProposal).isNotNull
        assertThat(savedProposal.proposerId).isEqualTo(host.id)

        // 호스트 한 명에게만 제안이 발행되었는지 확인
        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).hasSize(1)
        assertThat(proposalMembers.first().memberId).isEqualTo(host.id)
    }

    @Test
    fun `모임에 접근 권한이 없는 멤버가 제안을 발행하면 예외가 발생한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val nonMember = memberFixture.createMember(socialId = "nonMember", email = "nonMember@test.com")
        
        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val command = ProposalCommand.Propose(
            memberId = nonMember.id, // 모임에 속하지 않은 멤버
            gatheringId = gathering.id,
            purpose = ProposalPurposeInfo(
                whereTag = "카페",
                whenTag = "오후 3시",
                whatTag = "커피 모임"
            )
        )

        // when & then
        assertThrows<BaseException> {
            proposalCreateService.propose(command)
        }

        // 제안이 생성되지 않았는지 확인
        val proposals = proposalRepository.findAll()
        assertThat(proposals).isEmpty()

        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).isEmpty()
    }

    @Test
    fun `존재하지 않는 모임에 제안을 발행하면 예외가 발생한다`() {
        // given
        val member = memberFixture.createMember()
        val nonExistentGatheringId = 999999L

        val command = ProposalCommand.Propose(
            memberId = member.id,
            gatheringId = nonExistentGatheringId,
            purpose = ProposalPurposeInfo(
                whereTag = "카페",
                whenTag = "오후 3시",
                whatTag = "커피 모임"
            )
        )

        // when & then
        assertThrows<BaseException> {
            proposalCreateService.propose(command)
        }

        // 제안이 생성되지 않았는지 확인
        val proposals = proposalRepository.findAll()
        assertThat(proposals).isEmpty()

        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).isEmpty()
    }

    @Test
    fun `다양한 목적으로 제안을 발행할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering = gatheringFixture.createGathering(host, "다목적 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val command = ProposalCommand.Propose(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = ProposalPurposeInfo(
                whereTag = "강남역 스타벅스 2층",
                whenTag = "2024년 12월 25일 오후 2시 30분",
                whatTag = "크리스마스 파티 및 연말 회식"
            )
        )

        // when
        val result = proposalCreateService.propose(command)

        // then
        val savedProposal = proposalRepository.findById(result.proposalId).orElse(null)
        assertThat(savedProposal.purpose).isEqualTo("강남역 스타벅스 2층\n2024년 12월 25일 오후 2시 30분\n크리스마스 파티 및 연말 회식")
    }

    @Test
    fun `여러 번 제안을 발행할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering = gatheringFixture.createGathering(host, "정기 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val command1 = ProposalCommand.Propose(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = ProposalPurposeInfo(whereTag = "카페", whenTag = "오후 3시", whatTag = "첫 번째 모임")
        )

        val command2 = ProposalCommand.Propose(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = ProposalPurposeInfo(whereTag = "레스토랑", whenTag = "저녁 7시", whatTag = "두 번째 모임")
        )

        // when
        val result1 = proposalCreateService.propose(command1)
        val result2 = proposalCreateService.propose(command2)

        // then
        assertThat(result1.proposalId).isNotEqualTo(result2.proposalId)

        // 두 개의 독립적인 제안이 생성되었는지 확인
        val proposals = proposalRepository.findAll()
        assertThat(proposals).hasSize(2)

        // 각 제안마다 2명씩 제안 멤버가 생성되었는지 확인 (총 4개)
        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).hasSize(4)

        // 각 제안별로 멤버 수 확인
        val proposal1Members = proposalMembers.filter { it.proposal.id == result1.proposalId }
        val proposal2Members = proposalMembers.filter { it.proposal.id == result2.proposalId }
        
        assertThat(proposal1Members).hasSize(2)
        assertThat(proposal2Members).hasSize(2)
    }
}