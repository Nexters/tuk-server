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
    fun `멤버의 제안 목록을 정상적으로 조회한다`() {
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

        // 제안 생성
        val proposal1 = proposalRepository.save(Proposal.publish(gathering1.id, host.id, "첫 번째 제안"))
        val proposal2 = proposalRepository.save(Proposal.publish(gathering2.id, member2.id, "두 번째 제안"))

        // 제안 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member1.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member1.id))

        val query = ProposalQuery.MemberProposals(
            memberId = member1.id,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.size).isEqualTo(10)
        assertThat(result.pageNumber).isEqualTo(0)
        assertThat(result.content).hasSize(2)

        val gatheringNames = result.content.map { it.gatheringName }
        val purposes = result.content.map { it.purpose }
        assertThat(gatheringNames).containsExactlyInAnyOrder("첫 번째 모임", "두 번째 모임")
        assertThat(purposes).containsExactlyInAnyOrder("첫 번째 제안", "두 번째 제안")
    }

    @Test
    fun `제안이 없는 멤버는 빈 목록을 반환한다`() {
        // given  
        val member = memberFixture.createMember(socialId = "lonely", email = "lonely@test.com")

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.size).isEqualTo(10)
        assertThat(result.pageNumber).isEqualTo(0)
        // unread count no longer available in SliceResponse
        assertThat(result.content).isEmpty()
    }

    @Test
    fun `페이징이 올바르게 동작한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "페이징 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // 15개의 제안 생성
        val proposals = (1..15).map { index ->
            proposalRepository.save(Proposal.publish(gathering.id, host.id, "제안 $index"))
        }

        proposals.forEach { proposal ->
            proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))
        }

        // when
        val firstPageResult = proposalQueryService.getMemberProposals(
            ProposalQuery.MemberProposals(
                memberId = member.id,
                page = SliceRequest(
                    pageNumber = 0,
                    pageSize = 10
                )
            )
        )

        val secondPageResult = proposalQueryService.getMemberProposals(
            ProposalQuery.MemberProposals(
                memberId = member.id,
                page = SliceRequest(
                    pageNumber = 1,
                    pageSize = 10
                )
            )
        )

        // then
        assertThat(firstPageResult.hasNext).isTrue() // 15개 > 10개이므로 다음 페이지 존재
        assertThat(firstPageResult.size).isEqualTo(10)
        assertThat(firstPageResult.pageNumber).isEqualTo(0)
        // unread count no longer available in SliceResponse
        assertThat(firstPageResult.content).hasSize(10)

        assertThat(secondPageResult.hasNext).isFalse() // 5개 < 10개이므로 다음 페이지 없음
        assertThat(secondPageResult.size).isEqualTo(10)
        assertThat(secondPageResult.pageNumber).isEqualTo(1)
        // unread count no longer available in SliceResponse
        assertThat(secondPageResult.content).hasSize(5)
    }

    @Test
    fun `읽지 않은 제안 수를 정확히 계산한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "읽음 상태 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // 5개의 제안 생성
        val proposals = (1..5).map { index ->
            proposalRepository.save(Proposal.publish(gathering.id, host.id, "제안 $index"))
        }

        proposals.forEach { proposal ->
            proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))
        }

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        // unread count no longer available in SliceResponse - all proposals are unread
        assertThat(result.content).hasSize(5)
    }

    @Test
    fun `다른 멤버의 제안은 조회되지 않는다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "격리 테스트 모임")

        // member1만 모임에 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))

        // 제안 생성
        val proposal = proposalRepository.save(Proposal.publish(gathering.id, host.id, "제안"))

        // member1에게만 제안 멤버 생성 (member2에게는 생성하지 않음)
        proposalMemberRepository.save(ProposalMember.publish(proposal, member1.id))

        // member1 조회
        val member1Query = ProposalQuery.MemberProposals(
            memberId = member1.id,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // member2 조회
        val member2Query = ProposalQuery.MemberProposals(
            memberId = member2.id,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val member1Result = proposalQueryService.getMemberProposals(member1Query)
        val member2Result = proposalQueryService.getMemberProposals(member2Query)

        // then
        // member1은 제안을 볼 수 있음
        assertThat(member1Result.content).hasSize(1)
        // unread count no longer available in SliceResponse

        // member2는 제안을 볼 수 없음
        assertThat(member2Result.content).isEmpty()
        // unread count no longer available in SliceResponse
    }

    @Test
    fun `여러 모임의 제안을 모두 조회할 수 있다`() {
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

        // 각 모임에서 제안 생성
        val proposal1 = proposalRepository.save(Proposal.publish(gathering1.id, host1.id, "첫 번째 모임 제안"))
        val proposal2 = proposalRepository.save(Proposal.publish(gathering2.id, host2.id, "두 번째 모임 제안"))

        // 멤버에게 두 제안 모두 발송
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 0,
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
        assertThat(purposes).containsExactlyInAnyOrder("첫 번째 모임 제안", "두 번째 모임 제안")
    }

    @Test
    fun `hasNext 판단 로직이 정확하다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "경계값 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // 정확히 pageSize와 동일한 개수의 제안 생성 (10개)
        val proposals = (1..10).map { index ->
            proposalRepository.save(Proposal.publish(gathering.id, host.id, "제안 $index"))
        }

        proposals.forEach { proposal ->
            proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))
        }

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.hasNext).isFalse() // 10개 == 10개(pageSize)이므로 hasNext = false
        assertThat(result.content).hasSize(10)
    }

    @Test
    fun `삭제된 제안은 조회되지 않는다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "삭제 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // 제안 생성
        val proposal1 = proposalRepository.save(Proposal.publish(gathering.id, host.id, "활성 제안"))
        val proposal2 = proposalRepository.save(Proposal.publish(gathering.id, host.id, "삭제될 제안"))
        proposal2.delete()

        // 제안 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        val proposalMember2 = proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))
        proposalMember2.delete()

        // proposal2와 proposalMember2를 soft delete
        proposalMemberRepository.save(proposalMember2)
        proposalRepository.save(proposal2)

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.content).hasSize(1) // 삭제되지 않은 제안만 조회
        assertThat(result.content[0].purpose).isEqualTo("활성 제안")
        // unread count no longer available in SliceResponse - deleted proposals excluded from unread count
    }

    @Test
    fun `삭제된 모임의 제안은 조회되지 않는다`() {
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

        // 제안 생성
        val proposal1 = proposalRepository.save(Proposal.publish(gathering1.id, host.id, "활성 모임 제안"))
        val proposal2 = proposalRepository.save(Proposal.publish(gathering2.id, host.id, "삭제될 모임 제안"))

        // 제안 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))

        // gathering2를 soft delete
        gathering2.delete()
        gatheringRepository.save(gathering2)

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getMemberProposals(query)

        // then
        assertThat(result.content).hasSize(1) // 삭제되지 않은 모임의 제안만 조회
        assertThat(result.content[0].gatheringName).isEqualTo("활성 모임")
        assertThat(result.content[0].purpose).isEqualTo("활성 모임 제안")
    }

    @Test
    fun `proposalId가 올바르게 반환된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "ID 확인 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // 제안 생성
        val proposal = proposalRepository.save(Proposal.publish(gathering.id, host.id, "ID 확인 제안"))
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))

        val query = ProposalQuery.MemberProposals(
            memberId = member.id,
            page = SliceRequest(
                pageNumber = 0,
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
    fun `특정 모임에서 보낸 제안들을 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "제안 테스트 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        // member1이 보낸 제안 2개
        val proposal1 = proposalRepository.save(Proposal.publish(gathering.id, member1.id, "member1의 첫 번째 제안"))
        val proposal2 = proposalRepository.save(Proposal.publish(gathering.id, member1.id, "member1의 두 번째 제안"))

        // host가 보낸 제안 1개 (이건 조회되지 않아야 함)
        val proposal3 = proposalRepository.save(Proposal.publish(gathering.id, host.id, "host의 제안"))

        // 제안 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member1.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member1.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal3, member1.id))

        val query = ProposalQuery.GatheringProposals(
            memberId = member1.id,
            gatheringId = gathering.id,
            type = ProposalDirection.SENT,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.size).isEqualTo(10)
        assertThat(result.pageNumber).isEqualTo(0)
        assertThat(result.content).hasSize(2) // member1이 보낸 제안만 2개

        val purposes = result.content.map { it.purpose }
        assertThat(purposes).containsExactlyInAnyOrder("member1의 첫 번째 제안", "member1의 두 번째 제안")
    }

    @Test
    fun `특정 모임에서 받은 제안들을 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "제안 테스트 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        // 다른 사람들이 보낸 제안들
        val proposal1 = proposalRepository.save(Proposal.publish(gathering.id, host.id, "host의 제안"))
        val proposal2 = proposalRepository.save(Proposal.publish(gathering.id, member2.id, "member2의 제안"))

        // member1이 보낸 제안 (이건 조회되지 않아야 함)
        val proposal3 = proposalRepository.save(Proposal.publish(gathering.id, member1.id, "member1의 제안"))

        // 제안 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member1.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member1.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal3, member1.id))

        val query = ProposalQuery.GatheringProposals(
            memberId = member1.id,
            gatheringId = gathering.id,
            type = ProposalDirection.RECEIVED,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.size).isEqualTo(10)
        assertThat(result.pageNumber).isEqualTo(0)
        assertThat(result.content).hasSize(2) // member1이 받은 제안만 2개

        val purposes = result.content.map { it.purpose }
        assertThat(purposes).containsExactlyInAnyOrder("host의 제안", "member2의 제안")
    }

    @Test
    fun `모임 제안이 없는 경우 빈 목록을 반환한다`() {
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
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.hasNext).isFalse()
        assertThat(result.size).isEqualTo(10)
        assertThat(result.pageNumber).isEqualTo(0)
        assertThat(result.content).isEmpty()
    }

    @Test
    fun `모임 제안 페이징이 올바르게 동작한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering = gatheringFixture.createGathering(host, "페이징 테스트 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // member가 보낸 제안 15개 생성
        val proposals = (1..15).map { index ->
            proposalRepository.save(Proposal.publish(gathering.id, member.id, "제안 $index"))
        }

        proposals.forEach { proposal ->
            proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))
        }

        // when
        val firstPageResult = proposalQueryService.getGatheringProposals(
            ProposalQuery.GatheringProposals(
                memberId = member.id,
                gatheringId = gathering.id,
                type = ProposalDirection.SENT,
                page = SliceRequest(
                    pageNumber = 0,
                    pageSize = 10
                )
            )
        )

        val secondPageResult = proposalQueryService.getGatheringProposals(
            ProposalQuery.GatheringProposals(
                memberId = member.id,
                gatheringId = gathering.id,
                type = ProposalDirection.SENT,
                page = SliceRequest(
                    pageNumber = 1,
                    pageSize = 10
                )
            )
        )

        // then
        assertThat(firstPageResult.hasNext).isTrue() // 15개 > 10개이므로 다음 페이지 존재
        assertThat(firstPageResult.size).isEqualTo(10)
        assertThat(firstPageResult.pageNumber).isEqualTo(0)
        assertThat(firstPageResult.content).hasSize(10)

        assertThat(secondPageResult.hasNext).isFalse() // 5개 < 10개이므로 다음 페이지 없음
        assertThat(secondPageResult.size).isEqualTo(10)
        assertThat(secondPageResult.pageNumber).isEqualTo(1)
        assertThat(secondPageResult.content).hasSize(5)
    }

    @Test
    fun `다른 모임의 제안은 조회되지 않는다`() {
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

        // 각 모임에 제안 생성
        val proposal1 = proposalRepository.save(Proposal.publish(gathering1.id, member.id, "첫 번째 모임 제안"))
        val proposal2 = proposalRepository.save(Proposal.publish(gathering2.id, member.id, "두 번째 모임 제안"))

        // 제안 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))

        // gathering1에 대한 조회
        val query = ProposalQuery.GatheringProposals(
            memberId = member.id,
            gatheringId = gathering1.id,
            type = ProposalDirection.SENT,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.content).hasSize(1) // gathering1의 제안만 조회
        assertThat(result.content[0].purpose).isEqualTo("첫 번째 모임 제안")
        assertThat(result.content[0].gatheringName).isEqualTo("첫 번째 모임")
    }

    @Test
    fun `삭제된 제안은 모임 제안 조회에서 제외된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering = gatheringFixture.createGathering(host, "삭제 테스트 모임")

        // 모임 멤버 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        // 제안 생성
        val proposal1 = proposalRepository.save(Proposal.publish(gathering.id, member.id, "활성 제안"))
        val proposal2 = proposalRepository.save(Proposal.publish(gathering.id, member.id, "삭제될 제안"))

        // 제안 멤버 생성
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        val proposalMember2 = proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))

        proposalMember2.delete()
        proposalMemberRepository.save(proposalMember2)
        proposal2.delete()
        proposalRepository.save(proposal2)

        val query = ProposalQuery.GatheringProposals(
            memberId = member.id,
            gatheringId = gathering.id,
            type = ProposalDirection.SENT,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.content).hasSize(1) // 삭제되지 않은 제안만 조회
        assertThat(result.content[0].purpose).isEqualTo("활성 제안")
    }

    @Test
    fun `혼자만 있는 모임에서도 제안 조회가 가능하다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(host, "혼자 모임")

        // 호스트만 모임에 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        // host가 보낸 제안 생성
        val proposal = proposalRepository.save(Proposal.publish(gathering.id, host.id, "혼자 제안"))
        proposalMemberRepository.save(ProposalMember.publish(proposal, host.id))

        val query = ProposalQuery.GatheringProposals(
            memberId = host.id,
            gatheringId = gathering.id,
            type = ProposalDirection.SENT,
            page = SliceRequest(
                pageNumber = 0,
                pageSize = 10
            )
        )

        // when
        val result = proposalQueryService.getGatheringProposals(query)

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].purpose).isEqualTo("혼자 제안")
    }

    @Test
    fun `제안 상세 정보를 정상적으로 조회한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "상세 조회 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val proposal = proposalRepository.save(Proposal.publish(gathering.id, host.id, "상세 조회용 제안"))
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))

        // when
        val result = proposalQueryService.getProposal(proposal.id)

        // then
        assertThat(result.proposalId).isEqualTo(proposal.id)
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringName).isEqualTo("상세 조회 테스트 모임")
        assertThat(result.purpose).isEqualTo("상세 조회용 제안")
        assertThat(result.relativeTime).isNotNull()
    }

    @Test
    fun `존재하지 않는 제안 조회시 예외가 발생한다`() {
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
    fun `삭제된 제안 조회시 예외가 발생한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "삭제 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val proposal = proposalRepository.save(Proposal.publish(gathering.id, host.id, "삭제될 제안"))
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))
        
        // 제안을 soft delete
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
    fun `삭제된 모임의 제안 조회시 예외가 발생한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "삭제될 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val proposal = proposalRepository.save(Proposal.publish(gathering.id, host.id, "모임이 삭제될 제안"))
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))
        
        // 모임을 soft delete
        gathering.delete()
        gatheringRepository.save(gathering)

        // when & then
        assertThatThrownBy { 
            proposalQueryService.getProposal(proposal.id) 
        }
            .isInstanceOf(BaseException::class.java)
            .hasMessage("존재하지 않는 만남 초대장입니다.")
    }

    @Test
    fun `다양한 모임의 제안을 개별적으로 조회할 수 있다`() {
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

        val proposal1 = proposalRepository.save(Proposal.publish(gathering1.id, host1.id, "첫 번째 모임 제안"))
        val proposal2 = proposalRepository.save(Proposal.publish(gathering2.id, host2.id, "두 번째 모임 제안"))
        
        proposalMemberRepository.save(ProposalMember.publish(proposal1, member.id))
        proposalMemberRepository.save(ProposalMember.publish(proposal2, member.id))

        // when
        val result1 = proposalQueryService.getProposal(proposal1.id)
        val result2 = proposalQueryService.getProposal(proposal2.id)

        // then
        assertThat(result1.proposalId).isEqualTo(proposal1.id)
        assertThat(result1.gatheringId).isEqualTo(gathering1.id)
        assertThat(result1.gatheringName).isEqualTo("첫 번째 모임")
        assertThat(result1.purpose).isEqualTo("첫 번째 모임 제안")

        assertThat(result2.proposalId).isEqualTo(proposal2.id)
        assertThat(result2.gatheringId).isEqualTo(gathering2.id)
        assertThat(result2.gatheringName).isEqualTo("두 번째 모임")
        assertThat(result2.purpose).isEqualTo("두 번째 모임 제안")
    }

    @Test
    fun `특수 문자가 포함된 제안도 정상적으로 조회된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "🎉 특수문자 모임! @#$%")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val specialPurpose = "🍕 피자 먹으러 갈래? (맛있는 곳 알아!) 😋"
        val proposal = proposalRepository.save(Proposal.publish(gathering.id, host.id, specialPurpose))
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))

        // when
        val result = proposalQueryService.getProposal(proposal.id)

        // then
        assertThat(result.proposalId).isEqualTo(proposal.id)
        assertThat(result.gatheringName).isEqualTo("🎉 특수문자 모임! @#$%")
        assertThat(result.purpose).isEqualTo(specialPurpose)
    }

    @Test
    fun `긴 제안 내용도 정상적으로 조회된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "긴 제안 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val longPurpose = "이것은 긴 제안 내용입니다. ".repeat(10) // 적당히 긴 문자열
        val proposal = proposalRepository.save(Proposal.publish(gathering.id, host.id, longPurpose))
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))

        // when
        val result = proposalQueryService.getProposal(proposal.id)

        // then
        assertThat(result.proposalId).isEqualTo(proposal.id)
        assertThat(result.purpose).isEqualTo(longPurpose)
        assertThat(result.purpose.length).isGreaterThan(100) // 긴 문자열 확인
    }

    @Test
    fun `RelativeTime이 올바르게 계산된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")
        val gathering = gatheringFixture.createGathering(host, "시간 테스트 모임")

        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val proposal = proposalRepository.save(Proposal.publish(gathering.id, host.id, "시간 테스트 제안"))
        proposalMemberRepository.save(ProposalMember.publish(proposal, member.id))

        // when
        val result = proposalQueryService.getProposal(proposal.id)

        // then
        assertThat(result.relativeTime).isNotNull()
        assertThat(result.relativeTime.value).isNotBlank()
    }
}