package nexters.tuk.application.invitation

import nexters.tuk.application.invitation.dto.request.InvitationCommand
import nexters.tuk.application.invitation.vo.InvitationPurpose
import nexters.tuk.contract.BaseException
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.invitation.InvitationMemberRepository
import nexters.tuk.domain.invitation.InvitationRepository
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
class InvitationGenerateServiceIntegrationTest @Autowired constructor(
    private val invitationGenerateService: InvitationGenerateService,
    private val invitationRepository: InvitationRepository,
    private val invitationMemberRepository: InvitationMemberRepository,
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
    private val memberRepository: MemberRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)
    private val gatheringFixture =
        GatheringFixtureHelper(gatheringRepository, gatheringMemberRepository)

    @AfterEach
    fun tearDown() {
        invitationMemberRepository.deleteAllInBatch()
        invitationRepository.deleteAllInBatch()
        gatheringMemberRepository.deleteAllInBatch()
        gatheringRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `초대장을 성공적으로 발행한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")

        // 호스트와 멤버들을 모임에 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member1.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member2.id))

        val command = InvitationCommand.Publish(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = InvitationPurpose(
                where = "카페",
                time = "오후 3시",
                what = "커피 모임"
            )
        )

        // when
        val result = invitationGenerateService.publishInvitation(command)
        // then
        assertThat(result.invitationId).isNotNull()

        // 초대장이 생성되었는지 확인
        val savedInvitation = invitationRepository.findById(result.invitationId).orElse(null)
        assertThat(savedInvitation).isNotNull
        assertThat(savedInvitation.inviterId).isEqualTo(host.id)
        assertThat(savedInvitation.gatheringId).isEqualTo(gathering.id)
        assertThat(savedInvitation.purpose).isEqualTo("카페\n오후 3시\n커피 모임")

        // 모든 모임 멤버에게 초대장이 발행되었는지 확인 (총 3명)
        val invitationMembers = invitationMemberRepository.findAll()
        assertThat(invitationMembers).hasSize(3)

        val invitationMemberIds = invitationMembers.map { it.memberId }
        assertThat(invitationMemberIds).containsExactlyInAnyOrder(host.id, member1.id, member2.id)

        // 모든 초대 멤버가 같은 초대장에 연결되었는지 확인
        invitationMembers.forEach { invitationMember ->
            assertThat(invitationMember.invitation.id).isEqualTo(result.invitationId)
            assertThat(invitationMember.isRead).isFalse()
        }
    }

    @Test
    fun `혼자만 있는 모임에서도 초대장을 발행할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val gathering = gatheringFixture.createGathering(host, "혼자 모임")

        // 호스트만 모임에 등록
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val command = InvitationCommand.Publish(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = InvitationPurpose(
                where = "집",
                time = "저녁 7시",
                what = "혼자 공부"
            )
        )

        // when
        val result = invitationGenerateService.publishInvitation(command)

        // then
        assertThat(result.invitationId).isNotNull()

        val savedInvitation = invitationRepository.findById(result.invitationId).orElse(null)
        assertThat(savedInvitation).isNotNull
        assertThat(savedInvitation.inviterId).isEqualTo(host.id)

        // 호스트 한 명에게만 초대장이 발행되었는지 확인
        val invitationMembers = invitationMemberRepository.findAll()
        assertThat(invitationMembers).hasSize(1)
        assertThat(invitationMembers.first().memberId).isEqualTo(host.id)
    }

    @Test
    fun `모임에 접근 권한이 없는 멤버가 초대장을 발행하면 예외가 발생한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val nonMember =
            memberFixture.createMember(socialId = "nonMember", email = "nonMember@test.com")

        val gathering = gatheringFixture.createGathering(host, "테스트 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))

        val command = InvitationCommand.Publish(
            memberId = nonMember.id, // 모임에 속하지 않은 멤버
            gatheringId = gathering.id,
            purpose = InvitationPurpose(
                where = "카페",
                time = "오후 3시",
                what = "커피 모임"
            )
        )

        // when & then
        assertThrows<BaseException> {
            invitationGenerateService.publishInvitation(command)
        }

        // 초대장이 생성되지 않았는지 확인
        val invitations = invitationRepository.findAll()
        assertThat(invitations).isEmpty()

        val invitationMembers = invitationMemberRepository.findAll()
        assertThat(invitationMembers).isEmpty()
    }

    @Test
    fun `존재하지 않는 모임에 초대장을 발행하면 예외가 발생한다`() {
        // given
        val member = memberFixture.createMember()
        val nonExistentGatheringId = 999999L

        val command = InvitationCommand.Publish(
            memberId = member.id,
            gatheringId = nonExistentGatheringId,
            purpose = InvitationPurpose(
                where = "카페",
                time = "오후 3시",
                what = "커피 모임"
            )
        )

        // when & then
        assertThrows<BaseException> {
            invitationGenerateService.publishInvitation(command)
        }

        // 초대장이 생성되지 않았는지 확인
        val invitations = invitationRepository.findAll()
        assertThat(invitations).isEmpty()

        val invitationMembers = invitationMemberRepository.findAll()
        assertThat(invitationMembers).isEmpty()
    }

    @Test
    fun `다양한 목적으로 초대장을 발행할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering = gatheringFixture.createGathering(host, "다목적 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val command = InvitationCommand.Publish(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = InvitationPurpose(
                where = "강남역 스타벅스 2층",
                time = "2024년 12월 25일 오후 2시 30분",
                what = "크리스마스 파티 및 연말 회식"
            )
        )

        // when
        val result = invitationGenerateService.publishInvitation(command)

        // then
        val savedInvitation = invitationRepository.findById(result.invitationId).orElse(null)
        assertThat(savedInvitation.purpose).isEqualTo("강남역 스타벅스 2층\n2024년 12월 25일 오후 2시 30분\n크리스마스 파티 및 연말 회식")
    }

    @Test
    fun `여러 번 초대장을 발행할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val gathering = gatheringFixture.createGathering(host, "정기 모임")
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, host.id))
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))

        val command1 = InvitationCommand.Publish(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = InvitationPurpose(where = "카페", time = "오후 3시", what = "첫 번째 모임")
        )

        val command2 = InvitationCommand.Publish(
            memberId = host.id,
            gatheringId = gathering.id,
            purpose = InvitationPurpose(where = "레스토랑", time = "저녁 7시", what = "두 번째 모임")
        )

        // when
        val result1 = invitationGenerateService.publishInvitation(command1)
        val result2 = invitationGenerateService.publishInvitation(command2)

        // then
        assertThat(result1.invitationId).isNotEqualTo(result2.invitationId)

        // 두 개의 독립적인 초대장이 생성되었는지 확인
        val invitations = invitationRepository.findAll()
        assertThat(invitations).hasSize(2)

        // 각 초대장마다 2명씩 초대 멤버가 생성되었는지 확인 (총 4개)
        val invitationMembers = invitationMemberRepository.findAll()
        assertThat(invitationMembers).hasSize(4)

        // 각 초대장별로 멤버 수 확인
        val invitation1Members =
            invitationMembers.filter { it.invitation.id == result1.invitationId }
        val invitation2Members =
            invitationMembers.filter { it.invitation.id == result2.invitationId }

        assertThat(invitation1Members).hasSize(2)
        assertThat(invitation2Members).hasSize(2)
    }
}