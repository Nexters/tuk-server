package nexters.tuk.application.proposal

import nexters.tuk.application.proposal.dto.request.ProposalCommand
import nexters.tuk.application.proposal.vo.ProposalPurposeInfo
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.member.MemberRepository
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
            purpose = ProposalPurposeInfo(
                whereTag = "카페",
                whenTag = "오후 3시",
                whatTag = "커피 모임"
            )
        )

        // when
        val result = proposalService.propose(command)

        // then
        assertThat(result.proposalId).isNotNull()

        val savedProposal = proposalRepository.findById(result.proposalId).orElse(null)
        assertThat(savedProposal).isNotNull
        assertThat(savedProposal.proposerId).isEqualTo(host.id)
        assertThat(savedProposal.gatheringId).isNull()
        assertThat(savedProposal.purpose).isEqualTo("카페\n오후 3시\n커피 모임")
    }
}