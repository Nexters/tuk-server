package nexters.tuk.application.proposal

import nexters.tuk.application.proposal.dto.request.ProposalQuery
import nexters.tuk.contract.BaseException
import nexters.tuk.contract.SliceDto.SliceRequest
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.domain.proposal.Proposal
import nexters.tuk.domain.proposal.ProposalMember
import nexters.tuk.domain.proposal.ProposalMemberRepository
import nexters.tuk.domain.proposal.ProposalRepository
import nexters.tuk.fixtures.GatheringFixtureHelper
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ProposalQueryServiceIntegrationTest @Autowired constructor(
    private val proposalQueryService: ProposalQueryService,
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
    fun `멤버의 만남 초대장 목록을 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering1 = gatheringFixture.createGathering(host, "첫 번째 모임")
        val gathering2 = gatheringFixture.createGathering(host, "두 번째 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, member1.id))

        // 만남 초대장 생성
        val proposal1 = Proposal.publish(host.id, "첫 번째 만남 초대장")
        proposal1.registerGathering(gathering1.id)
        proposalRepository.save(proposal1)

        val proposal2 = Proposal.publish(member2.id, "두 번째 만남 초대장")
        proposal2.registerGathering(gathering2.id)
        proposalRepository.save(proposal2)

        // 만남 초대장 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member1.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member1.id))

        val query = ProposalQuery.MemberProposals(
            memberId = member1.id,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.size).isEqualTo(10)
        assertThat(result.pageNumber).isEqualTo(1)
        assertThat(result.content).hasSize(2)

        val gatheringNames = result.content.map { it.gatheringName }
        val purposes = result.content.map { it.purpose }
        assertThat(gatheringNames).containsExactlyInAnyOrder("첫 번째 모임", "두 번째 모임")
        assertThat(purposes).containsExactlyInAnyOrder("첫 번째 만남 초대장", "두 번째 만남 초대장")
    }

    @Test
    fun `만남 초대장이 없는 멤버는 빈 목록을 반환한다`() {
        // given  
        val member = memberFixture.createMember(socialId = "lonely", email = "lonely@test.com")

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.size).isEqualTo(10)
        assertThat(result.pageNumber).isEqualTo(1)
        // unread count no longer available in SliceResponse
        assertThat(result.content).isEmpty()
    }


    @Test
    fun `다른 멤버의 만남 초대장은 조회되지 않는다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "격리 테스트 모임")

        // member1만 모임에 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))

        // 만남 초대장 생성
        val proposal = Proposal.publish(host.id, "만남 초대장")
        proposal.registerGathering(gathering.id)
        proposalRepository.save(proposal)

        // member1에게만 만남 초대장 멤버 생성 (member2에게는 생성하지 않음)
        proposalMemberRepository.save(ProposalMember.publish(proposal, member1.id))

        // member1 조회
        val member1Query = ProposalQuery.MemberProposals(
            memberId = member1.id,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // member2 조회
        val member2Query = ProposalQuery.MemberProposals(
            memberId = member2.id,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val member1Result = proposalQueryService.getMemberProposals(member1Query)
        val member2Result = proposalQueryService.getMemberProposals(member2Query)

        // then
        // member1은 만남 초대장을 볼 수 있음
        assertThat(member1Result.content).hasSize(1)
        // unread count no longer available in SliceResponse

        // member2는 만남 초대장을 볼 수 없음
        assertThat(member2Result.content).isEmpty()
        // unread count no longer available in SliceResponse
    }

    @Test
    fun `여러 모임의 만남 초대장을 모두 조회할 수 있다`() {
        // given
        val host1 = memberFixture.createMember(socialId = "host1", email = "host1@test.com")
        val host2 = memberFixture.createMember(socialId = "host2", email = "host2@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering1 = gatheringFixture.createGathering(host1, "첫 번째 모임")
        val gathering2 = gatheringFixture.createGathering(host2, "두 번째 모임")

        // 멤버를 두 모임 모두에 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, host1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, member.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, host2.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, member.id))

        // 각 모임에서 만남 초대장 생성
        val proposal1 = Proposal.publish(host1.id, "첫 번째 모임 만남 초대장")
        proposal1.registerGathering(gathering1.id)
        proposalRepository.save(proposal1)

        val proposal2 = Proposal.publish(host2.id, "두 번째 모임 만남 초대장")
        proposal2.registerGathering(gathering2.id)
        proposalRepository.save(proposal2)

        // 멤버에게 두 만남 초대장 모두 발송
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.content).hasSize(2)
        // unread count no longer available in SliceResponse

        val gatheringNames = result.content.map { it.gatheringName }
        val purposes = result.content.map { it.purpose }
        assertThat(gatheringNames).containsExactlyInAnyOrder("첫 번째 모임", "두 번째 모임")
        assertThat(purposes).containsExactlyInAnyOrder("첫 번째 모임 만남 초대장", "두 번째 모임 만남 초대장")
    }


    @Test
    fun `삭제된 만남 초대장은 조회되지 않는다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "삭제 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // 만남 초대장 생성
        val proposal1 = Proposal.publish(host.id, "활성 만남 초대장")
        proposal1.registerGathering(gathering.id)
        proposalRepository.save(proposal1)

        val proposal2 = Proposal.publish(host.id, "삭제될 만남 초대장")
        proposal2.registerGathering(gathering.id)
        proposalRepository.save(proposal2)
        proposal2.delete()

        // 만남 초대장 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        val proposalMember2 =
            proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))
        proposalMember2.delete()

        // proposal2와 proposalMember2를 soft delete
        proposalMemberRepository.save(proposalMember2)
        proposalRepository.save(proposal2)

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.content).hasSize(1) // 삭제되지 않은 만남 초대장만 조회
        assertThat(result.content[0].purpose).isEqualTo("활성 만남 초대장")
        // unread count no longer available in SliceResponse - deleted proposals excluded from unread count
    }

    @Test
    fun `삭제된 모임의 만남 초대장은 조회되지 않는다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering1 = gatheringFixture.createGathering(host, "활성 모임")
        val gathering2 = gatheringFixture.createGathering(host, "삭제될 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, member.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, member.id))

        // 만남 초대장 생성
        val proposal1 = Proposal.publish(host.id, "활성 모임 만남 초대장")
        proposal1.registerGathering(gathering1.id)
        proposalRepository.save(proposal1)

        val proposal2 = Proposal.publish(host.id, "삭제될 모임 만남 초대장")
        proposal2.registerGathering(gathering2.id)
        proposalRepository.save(proposal2)

        // 만남 초대장 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))

        // gathering2를 soft delete
        gathering2.delete()
        gatheringRepository.save(gathering2)

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.content).hasSize(1) // 삭제되지 않은 모임의 만남 초대장만 조회
        assertThat(result.content[0].gatheringName).isEqualTo("활성 모임")
        assertThat(result.content[0].purpose).isEqualTo("활성 모임 만남 초대장")
    }

    @Test
    fun `proposalId가 올바르게 반환된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "ID 확인 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // 만남 초대장 생성
        val proposal = Proposal.publish(host.id, "ID 확인 만남 초대장")
        proposal.registerGathering(gathering.id)
        proposalRepository.save(proposal)
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].proposalId).isEqualTo(proposal.id)
        assertThat(result.content[0].relativeTime).isNotNull()
    }

    @Test
    fun `특정 모임에서 보낸 만남 초대장들을 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "만남 초대장 테스트 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        // member1이 보낸 만남 초대장 2개
        val proposal1 = Proposal.publish(member1.id, "member1의 첫 번째 만남 초대장")
        proposal1.registerGathering(gathering.id)
        proposalRepository.save(proposal1)

        val proposal2 = Proposal.publish(member1.id, "member1의 두 번째 만남 초대장")
        proposal2.registerGathering(gathering.id)
        proposalRepository.save(proposal2)

        // host가 보낸 만남 초대장 1개 (이건 조회되지 않아야 함)
        val proposal3 = Proposal.publish(host.id, "host의 만남 초대장")
        proposal3.registerGathering(gathering.id)
        proposalRepository.save(proposal3)

        // 만남 초대장 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member1.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member1.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal3, member1.id))

        val query = ProposalQuery.GatheringProposals(
            memberId = member1.id,
            gatheringId = gathering.id,
            type = ProposalDirection.SENT,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.size).isEqualTo(10)
        assertThat(result.pageNumber).isEqualTo(1)
        assertThat(result.content).hasSize(2) // member1이 보낸 만남 초대장만 2개

        val purposes = result.content.map { it.purpose }
        assertThat(purposes).containsExactlyInAnyOrder(
            "member1의 첫 번째 만남 초대장",
            "member1의 두 번째 만남 초대장"
        )
    }

    @Test
    fun `특정 모임에서 받은 만남 초대장들을 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "만남 초대장 테스트 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        // 다른 사람들이 보낸 만남 초대장들
        val proposal1 = Proposal.publish(host.id, "host의 만남 초대장")
        proposal1.registerGathering(gathering.id)
        proposalRepository.save(proposal1)

        val proposal2 = Proposal.publish(member2.id, "member2의 만남 초대장")
        proposal2.registerGathering(gathering.id)
        proposalRepository.save(proposal2)

        // member1이 보낸 만남 초대장 (이건 조회되지 않아야 함)
        val proposal3 = Proposal.publish(member1.id, "member1의 만남 초대장")
        proposal3.registerGathering(gathering.id)
        proposalRepository.save(proposal3)

        // 만남 초대장 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member1.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member1.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal3, member1.id))

        val query = ProposalQuery.GatheringProposals(
            memberId = member1.id,
            gatheringId = gathering.id,
            type = ProposalDirection.RECEIVED,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.size).isEqualTo(10)
        assertThat(result.pageNumber).isEqualTo(1)
        assertThat(result.content).hasSize(2) // member1이 받은 만남 초대장만 2개

        val purposes = result.content.map { it.purpose }
        assertThat(purposes).containsExactlyInAnyOrder("host의 만남 초대장", "member2의 만남 초대장")
    }

    @Test
    fun `모임 만남 초대장이 없는 경우 빈 목록을 반환한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering = gatheringFixture.createGathering(host, "빈 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val query = ProposalQuery.GatheringProposals(
            memberId = member.id,
            gatheringId = gathering.id,
            type = ProposalDirection.SENT,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.size).isEqualTo(10)
        assertThat(result.pageNumber).isEqualTo(1)
        assertThat(result.content).isEmpty()
    }


    @Test
    fun `다른 모임의 만남 초대장은 조회되지 않는다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering1 = gatheringFixture.createGathering(host, "첫 번째 모임")
        val gathering2 = gatheringFixture.createGathering(host, "두 번째 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, member.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, member.id))

        // 각 모임에 만남 초대장 생성
        val proposal1 = Proposal.publish(member.id, "첫 번째 모임 만남 초대장")
        proposal1.registerGathering(gathering1.id)
        proposalRepository.save(proposal1)

        val proposal2 = Proposal.publish(member.id, "두 번째 모임 만남 초대장")
        proposal2.registerGathering(gathering2.id)
        proposalRepository.save(proposal2)

        // 만남 초대장 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))

        // gathering1에 대한 조회
        val query = ProposalQuery.GatheringProposals(
            memberId = member.id,
            gatheringId = gathering1.id,
            type = ProposalDirection.SENT,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.content).hasSize(1) // gathering1의 만남 초대장만 조회
        assertThat(result.content[0].purpose).isEqualTo("첫 번째 모임 만남 초대장")
        assertThat(result.content[0].gatheringName).isEqualTo("첫 번째 모임")
    }

    @Test
    fun `삭제된 만남 초대장은 모임 만남 초대장 조회에서 제외된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering = gatheringFixture.createGathering(host, "삭제 테스트 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // 만남 초대장 생성
        val proposal1 = Proposal.publish(member.id, "활성 만남 초대장")
        proposal1.registerGathering(gathering.id)
        proposalRepository.save(proposal1)

        val proposal2 = Proposal.publish(member.id, "삭제될 만남 초대장")
        proposal2.registerGathering(gathering.id)
        proposalRepository.save(proposal2)

        // 만남 초대장 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        val proposalMember2 =
            proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))

        proposalMember2.delete()
        proposalMemberRepository.save(proposalMember2)
        proposal2.delete()
        proposalRepository.save(proposal2)

        val query = ProposalQuery.GatheringProposals(
            memberId = member.id,
            gatheringId = gathering.id,
            type = ProposalDirection.SENT,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.content).hasSize(1) // 삭제되지 않은 만남 초대장만 조회
        assertThat(result.content[0].purpose).isEqualTo("활성 만남 초대장")
    }

    @Test
    fun `혼자만 있는 모임에서도 만남 초대장 조회가 가능하다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(host, "혼자 모임")

        // 호스트만 모임에 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        // host가 보낸 만남 초대장 생성
        val proposal = Proposal.publish(host.id, "혼자 만남 초대장")
        proposal.registerGathering(gathering.id)
        proposalRepository.save(proposal)
        proposalMemberRepository.save(ProposalMember.publish(proposal, host.id))

        val query = ProposalQuery.GatheringProposals(
            memberId = host.id,
            gatheringId = gathering.id,
            type = ProposalDirection.SENT,
            page = SliceRequest(
                pageNumber = 1,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].purpose).isEqualTo("혼자 만남 초대장")
    }

    @Test
    fun `만남 초대장 상세 정보를 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "상세 조회 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val proposal = Proposal.publish(host.id, "상세 조회용 만남 초대장")
        proposal.registerGathering(gathering.id)
        proposalRepository.save(proposal)
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))

        // when
        val result = proposalQueryService.getProposal(proposal.id)

        // then
        assertThat(result.proposalId).isEqualTo(proposal.id)
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringName).isEqualTo("상세 조회 테스트 모임")
        assertThat(result.purpose).isEqualTo("상세 조회용 만남 초대장")
        assertThat(result.relativeTime).isNotNull()
    }

    @Test
    fun `존재하지 않는 만남 초대장 조회시 예외가 발생한다`() {
        // given
        val nonExistentProposalId = 999999L

        // when & then
        assertThatThrownBy {
            proposalQueryService.getProposal(nonExistentProposalId)
        }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("존재하지 않는 만남 초대장입니다.")
    }

    @Test
    fun `모임이 없는 만남 초대장도 조회할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")

        // 모임 없이 만남 초대장만 생성 (gatheringId = null)
        val proposal = Proposal.publish(host.id, "모임 없는 만남 초대장")
        // proposal.registerGathering()를 호출하지 않음 - gatheringId가 null인 상태
        proposalRepository.save(proposal)

        // when
        val result = proposalQueryService.getProposal(proposal.id)

        // then
        assertThat(result.proposalId).isEqualTo(proposal.id)
        assertThat(result.gatheringId).isNull() // 모임이 없으므로 null
        assertThat(result.gatheringName).isNull() // 모임이 없으므로 null
        assertThat(result.purpose).isEqualTo("모임 없는 만남 초대장")
        assertThat(result.relativeTime).isNotNull()
    }

    @Test
    fun `삭제된 만남 초대장 조회시 예외가 발생한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "삭제 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val proposal = Proposal.publish(host.id, "삭제될 만남 초대장")
        proposal.registerGathering(gathering.id)
        proposalRepository.save(proposal)
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))

        // 만남 초대장을 soft delete
        proposal.delete()
        proposalRepository.save(proposal)

        // when & then
        assertThatThrownBy {
            proposalQueryService.getProposal(proposal.id)
        }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("존재하지 않는 만남 초대장입니다.")
    }

    @Test
    fun `삭제된 모임의 만남 초대장도 조회할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "삭제될 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val proposal = Proposal.publish(host.id, "모임이 삭제될 만남 초대장")
        proposal.registerGathering(gathering.id)
        proposalRepository.save(proposal)
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))

        // 모임을 soft delete
        gathering.delete()
        gatheringRepository.save(gathering)

        // when
        val result = proposalQueryService.getProposal(proposal.id)

        // then
        assertThat(result.proposalId).isEqualTo(proposal.id)
        assertThat(result.gatheringId).isNull() // 삭제된 모임이므로 LEFT JOIN 결과가 null
        assertThat(result.gatheringName).isNull() // 삭제된 모임이므로 name은 null
        assertThat(result.purpose).isEqualTo("모임이 삭제될 만남 초대장")
        assertThat(result.relativeTime).isNotNull()
    }

    @Test
    fun `다양한 모임의 만남 초대장을 개별적으로 조회할 수 있다`() {
        // given
        val host1 = memberFixture.createMember(socialId = "host1", email = "host1@test.com")
        val host2 = memberFixture.createMember(socialId = "host2", email = "host2@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering1 = gatheringFixture.createGathering(host1, "첫 번째 모임")
        val gathering2 = gatheringFixture.createGathering(host2, "두 번째 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, host1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering1, member.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, host2.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering2, member.id))

        val proposal1 = Proposal.publish(host1.id, "첫 번째 모임 만남 초대장")
        proposal1.registerGathering(gathering1.id)
        proposalRepository.save(proposal1)

        val proposal2 = Proposal.publish(host2.id, "두 번째 모임 만남 초대장")
        proposal2.registerGathering(gathering2.id)
        proposalRepository.save(proposal2)

        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))

        // when
        val result1 = proposalQueryService.getProposal(proposal1.id)
        val result2 = proposalQueryService.getProposal(proposal2.id)

        // then
        assertThat(result1.proposalId).isEqualTo(proposal1.id)
        assertThat(result1.gatheringId).isEqualTo(gathering1.id)
        assertThat(result1.gatheringName).isEqualTo("첫 번째 모임")
        assertThat(result1.purpose).isEqualTo("첫 번째 모임 만남 초대장")

        assertThat(result2.proposalId).isEqualTo(proposal2.id)
        assertThat(result2.gatheringId).isEqualTo(gathering2.id)
        assertThat(result2.gatheringName).isEqualTo("두 번째 모임")
        assertThat(result2.purpose).isEqualTo("두 번째 모임 만남 초대장")
    }
}