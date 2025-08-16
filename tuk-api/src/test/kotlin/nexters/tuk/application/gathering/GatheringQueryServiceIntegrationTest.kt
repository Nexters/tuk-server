package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringQuery
import nexters.tuk.contract.BaseException
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
    fun `호스트가 모임 상세를 조회할 때 모든 정보가 올바르게 설정된다`() {
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
        val proposal1 = createProposalWithMembers(host.id, gathering.id, "모임 제안1", listOf(host.id, member1.id, member2.id))
        val proposal2 = createProposalWithMembers(host.id, gathering.id, "모임 제안2", listOf(host.id, member1.id, member2.id))
        val proposal3 = createProposalWithMembers(member1.id, gathering.id, "모임 제안3", listOf(host.id, member1.id, member2.id))

        val query = GatheringQuery.GatheringDetail(host.id, gathering.id)

        // when
        val result = gatheringQueryService.getGatheringDetail(query)

        // then - 기본 정보
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringIntervalDays).isEqualTo(gathering.intervalDays)
        assertThat(result.gatheringName).isEqualTo("테스트 모임")
        assertThat(result.lastPushRelativeTime).isNotNull()
        assertThat(result.sentProposalCount).isEqualTo(2)
        assertThat(result.receivedProposalCount).isEqualTo(1)
        assertThat(result.members).hasSize(3)
        assertThat(result.isHost).isTrue()

        // then - isMe와 isHost 검증
        val hostMember = result.members.find { it.memberId == host.id }
        assertThat(hostMember).isNotNull
        assertThat(hostMember!!.isMe).isTrue()
        assertThat(hostMember.isHost).isTrue()

        val member1Info = result.members.find { it.memberId == member1.id }
        assertThat(member1Info).isNotNull
        assertThat(member1Info!!.isMe).isFalse()
        assertThat(member1Info.isHost).isFalse()

        val member2Info = result.members.find { it.memberId == member2.id }
        assertThat(member2Info).isNotNull
        assertThat(member2Info!!.isMe).isFalse()
        assertThat(member2Info.isHost).isFalse()
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
        assertThat(result.gatheringIntervalDays).isEqualTo(gathering.intervalDays)
        assertThat(result.gatheringName).isEqualTo("제안 없는 모임")
        assertThat(result.lastPushRelativeTime).isNotNull()
        assertThat(result.sentProposalCount).isEqualTo(0)
        assertThat(result.receivedProposalCount).isEqualTo(0)
        assertThat(result.members).hasSize(2)
    }

    @Test
    fun `혼자만 있는 모임에서 호스트의 모든 정보가 올바르게 설정된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")

        val gathering = gatheringFixture.createGathering(host, "혼자 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val query = GatheringQuery.GatheringDetail(host.id, gathering.id)

        // when
        val result = gatheringQueryService.getGatheringDetail(query)

        // then - 기본 정보
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringIntervalDays).isEqualTo(gathering.intervalDays)
        assertThat(result.gatheringName).isEqualTo("혼자 모임")
        assertThat(result.lastPushRelativeTime).isNotNull()
        assertThat(result.sentProposalCount).isEqualTo(0)
        assertThat(result.receivedProposalCount).isEqualTo(0)
        assertThat(result.members).hasSize(1)
        assertThat(result.isHost).isTrue()

        // then - isMe와 isHost 검증
        val hostMember = result.members.first()
        assertThat(hostMember.memberId).isEqualTo(host.id)
        assertThat(hostMember.memberName).isEqualTo("테스트사용자")
        assertThat(hostMember.isMe).isTrue()
        assertThat(hostMember.isHost).isTrue()
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
        val proposal1 = createProposalWithMembers(host.id, gathering.id, "첫번째 제안", listOf(host.id, member1.id))
        val proposal2 = createProposalWithMembers(host.id, gathering.id, "두번째 제안", listOf(host.id, member1.id))
        val proposal3 = createProposalWithMembers(member1.id, gathering.id, "역제안", listOf(host.id, member1.id))

        val query = GatheringQuery.GatheringDetail(host.id, gathering.id)

        // when
        val result = gatheringQueryService.getGatheringDetail(query)

        // then
        assertThat(result.gatheringId).isEqualTo(gathering.id)
        assertThat(result.gatheringIntervalDays).isEqualTo(gathering.intervalDays)
        assertThat(result.lastPushRelativeTime).isNotNull()
        assertThat(result.sentProposalCount).isEqualTo(2) // 상태와 관계없이 보낸 초대장 수
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


    @Test
    fun `일반 멤버가 모임 상세를 조회할 때 isMe와 isHost가 올바르게 설정된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        val query = GatheringQuery.GatheringDetail(member1.id, gathering.id)

        // when
        val result = gatheringQueryService.getGatheringDetail(query)

        // then
        assertThat(result.isHost).isFalse()
        assertThat(result.members).hasSize(3)

        val hostMember = result.members.find { it.memberId == host.id }
        assertThat(hostMember).isNotNull
        assertThat(hostMember!!.isMe).isFalse()
        assertThat(hostMember.isHost).isTrue()

        val member1Info = result.members.find { it.memberId == member1.id }
        assertThat(member1Info).isNotNull
        assertThat(member1Info!!.isMe).isTrue()
        assertThat(member1Info.isHost).isFalse()

        val member2Info = result.members.find { it.memberId == member2.id }
        assertThat(member2Info).isNotNull
        assertThat(member2Info!!.isMe).isFalse()
        assertThat(member2Info.isHost).isFalse()
    }

    @Test
    fun `나중에 모임에 추가된 멤버의 통계가 올바르게 반영된다`() {
        // given - 기존 멤버들과 초대장들이 있는 상황
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val existingMember = memberFixture.createMember(socialId = "existing", email = "existing@test.com")
        val newMember = memberFixture.createMember(socialId = "new", email = "new@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        
        // 기존 멤버들만 모임에 추가
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, existingMember.id))

        // 기존 멤버들 간의 초대장 생성
        createProposalWithMembers(host.id, gathering.id, "기존 제안1", listOf(host.id, existingMember.id))
        createProposalWithMembers(existingMember.id, gathering.id, "기존 제안2", listOf(host.id, existingMember.id))

        // 새로운 멤버를 모임에 추가
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, newMember.id))

        // 새로운 멤버가 포함된 초대장 생성
        createProposalWithMembers(host.id, gathering.id, "새 멤버 포함 제안", listOf(host.id, existingMember.id, newMember.id))
        createProposalWithMembers(newMember.id, gathering.id, "새 멤버의 제안", listOf(host.id, existingMember.id, newMember.id))

        // when - 새로운 멤버의 모임 상세 조회
        val newMemberQuery = GatheringQuery.GatheringDetail(newMember.id, gathering.id)
        val result = gatheringQueryService.getGatheringDetail(newMemberQuery)

        // then - 새로운 멤버의 통계는 자신이 참여한 초대장만 반영되어야 함
        assertThat(result.sentProposalCount).isEqualTo(1) // 자신이 보낸 초대장 1개
        assertThat(result.receivedProposalCount).isEqualTo(1) // 자신이 받은 초대장 1개 (host가 새로 보낸 것)
        assertThat(result.members).hasSize(3) // host, existingMember, newMember
        assertThat(result.isHost).isFalse()

        // 기존 멤버들의 통계도 확인
        val hostQuery = GatheringQuery.GatheringDetail(host.id, gathering.id)
        val hostResult = gatheringQueryService.getGatheringDetail(hostQuery)
        
        assertThat(hostResult.sentProposalCount).isEqualTo(2) // 기존 1개 + 새로운 1개
        assertThat(hostResult.receivedProposalCount).isEqualTo(2) // existingMember가 보낸 1개 + newMember가 보낸 1개
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