package nexters.tuk.application.proposal

import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.vo.ProposalPurposeInfo
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.domain.proposal.ProposalMemberRepository
import nexters.tuk.domain.proposal.ProposalRepository
import nexters.tuk.fixtures.GatheringFixtureHelper
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
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
    private val gatheringFixture =
        GatheringFixtureHelper(gatheringRepository, gatheringMemberRepository)

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
            gatheringId = null, // 새로운 API에서는 gatheringId가 null
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
        assertThat(savedProposal.gatheringId).isNull() // 새로운 API에서는 gatheringId가 null
        assertThat(savedProposal.purpose).isEqualTo("카페\n오후 3시\n커피 모임")

        // 새로운 API에서는 ProposalCreateService가 모임 멤버에게 자동으로 발송하지 않음
        // ProposalMember는 GatheringProposalService.addProposal에서 생성됨
        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).hasSize(0) // 모임에 추가되기 전까지는 멤버가 없음
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
            gatheringId = null, // 새로운 API에서는 gatheringId가 null
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
            gatheringId = null, // 새로운 API에서는 gatheringId가 null
            purpose = ProposalPurposeInfo(whereTag = "카페", whenTag = "오후 3시", whatTag = "첫 번째 모임")
        )

        val command2 = ProposalCommand.Propose(
            memberId = host.id,
            gatheringId = null, // 새로운 API에서는 gatheringId가 null
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

        // 새로운 API에서는 ProposalCreateService가 자동으로 멤버에게 발송하지 않음
        val proposalMembers = proposalMemberRepository.findAll()
        assertThat(proposalMembers).hasSize(0)

        // 각 제안이 독립적으로 생성되고 gatheringId가 null인지 확인
        val savedProposals = proposalRepository.findAll()
        val proposal1 = savedProposals.find { it.id == result1.proposalId }
        val proposal2 = savedProposals.find { it.id == result2.proposalId }

        assertThat(proposal1?.gatheringId).isNull()
        assertThat(proposal2?.gatheringId).isNull()
    }
}