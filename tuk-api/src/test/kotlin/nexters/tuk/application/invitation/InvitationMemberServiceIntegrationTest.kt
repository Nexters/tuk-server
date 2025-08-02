package nexters.tuk.application.invitation

import nexters.tuk.contract.BaseException
import nexters.tuk.domain.invitation.Invitation
import nexters.tuk.domain.invitation.InvitationMemberRepository
import nexters.tuk.domain.invitation.InvitationRepository
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class InvitationMemberServiceIntegrationTest @Autowired constructor(
    private val invitationMemberService: InvitationMemberService,
    private val invitationRepository: InvitationRepository,
    private val invitationMemberRepository: InvitationMemberRepository,
    private val memberRepository: MemberRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)

    @AfterEach
    fun tearDown() {
        invitationMemberRepository.deleteAllInBatch()
        invitationRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `초대장에 멤버들을 성공적으로 발행한다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")
        val member3 = memberFixture.createMember(socialId = "member3", email = "member3@test.com")

        val invitation = invitationRepository.save(
            Invitation.publish(
                gatheringId = 1L,
                inviterId = host.id,
                purpose = "테스트 초대장"
            )
        )

        val memberIds = listOf(member1.id, member2.id, member3.id)

        // when
        val result = invitationMemberService.publishGatheringMembers(invitation.id, memberIds)

        // then
        assertThat(result.invitationMemberIds).hasSize(3)
        result.invitationMemberIds.forEach { invitationMemberId ->
            assertThat(invitationMemberId).isNotNull()
        }

        // 실제로 저장되었는지 확인
        val savedInvitationMembers = invitationMemberRepository.findAll()
        assertThat(savedInvitationMembers).hasSize(3)

        val savedMemberIds = savedInvitationMembers.map { it.memberId }
        assertThat(savedMemberIds).containsExactlyInAnyOrder(member1.id, member2.id, member3.id)

        // 초대장과 올바르게 연결되었는지 확인
        savedInvitationMembers.forEach { invitationMember ->
            assertThat(invitationMember.invitation.id).isEqualTo(invitation.id)
            assertThat(invitationMember.isRead).isFalse()
        }
    }

    @Test
    fun `단일 멤버에게 초대장을 발행할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member = memberFixture.createMember(socialId = "member", email = "member@test.com")

        val invitation = invitationRepository.save(
            Invitation.publish(
                gatheringId = 1L,
                inviterId = host.id,
                purpose = "단일 멤버 초대"
            )
        )

        val memberIds = listOf(member.id)

        // when
        val result = invitationMemberService.publishGatheringMembers(invitation.id, memberIds)

        // then
        assertThat(result.invitationMemberIds).hasSize(1)
        
        val savedInvitationMembers = invitationMemberRepository.findAll()
        assertThat(savedInvitationMembers).hasSize(1)
        assertThat(savedInvitationMembers.first().memberId).isEqualTo(member.id)
        assertThat(savedInvitationMembers.first().invitation.id).isEqualTo(invitation.id)
    }

    @Test
    fun `빈 멤버 목록으로 초대장을 발행하면 아무것도 생성되지 않는다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")

        val invitation = invitationRepository.save(
            Invitation.publish(
                gatheringId = 1L,
                inviterId = host.id,
                purpose = "빈 멤버 목록 테스트"
            )
        )

        val emptyMemberIds = emptyList<Long>()

        // when
        val result = invitationMemberService.publishGatheringMembers(invitation.id, emptyMemberIds)

        // then
        assertThat(result.invitationMemberIds).isEmpty()
        
        val savedInvitationMembers = invitationMemberRepository.findAll()
        assertThat(savedInvitationMembers).isEmpty()
    }

    @Test
    fun `존재하지 않는 초대장 ID로 멤버를 발행하면 예외가 발생한다`() {
        // given
        val member = memberFixture.createMember()
        val nonExistentInvitationId = 999999L
        val memberIds = listOf(member.id)

        // when & then
        val exception = assertThrows<BaseException> {
            invitationMemberService.publishGatheringMembers(nonExistentInvitationId, memberIds)
        }

        assertThat(exception.message).isEqualTo("찾을 수 없는 초대장입니다.")
    }

    @Test
    fun `중복된 멤버 ID가 있어도 모두 발행된다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val invitation = invitationRepository.save(
            Invitation.publish(
                gatheringId = 1L,
                inviterId = host.id,
                purpose = "중복 멤버 테스트"
            )
        )

        val memberIds = listOf(member1.id, member2.id, member1.id) // member1 중복

        // when
        val result = invitationMemberService.publishGatheringMembers(invitation.id, memberIds)

        // then
        assertThat(result.invitationMemberIds).hasSize(3) // 중복되어도 3개 생성
        
        val savedInvitationMembers = invitationMemberRepository.findAll()
        assertThat(savedInvitationMembers).hasSize(3)
        
        val savedMemberIds = savedInvitationMembers.map { it.memberId }
        assertThat(savedMemberIds).containsExactly(member1.id, member2.id, member1.id)
    }

    @Test
    fun `여러 초대장에 대해 독립적으로 멤버를 발행할 수 있다`() {
        // given
        val host = memberFixture.createMember(socialId = "host", email = "host@test.com")
        val member1 = memberFixture.createMember(socialId = "member1", email = "member1@test.com")
        val member2 = memberFixture.createMember(socialId = "member2", email = "member2@test.com")

        val invitation1 = invitationRepository.save(
            Invitation.publish(
                gatheringId = 1L,
                inviterId = host.id,
                purpose = "첫번째 초대장"
            )
        )

        val invitation2 = invitationRepository.save(
            Invitation.publish(
                gatheringId = 2L,
                inviterId = host.id,
                purpose = "두번째 초대장"
            )
        )

        // when
        val result1 = invitationMemberService.publishGatheringMembers(invitation1.id, listOf(member1.id))
        val result2 = invitationMemberService.publishGatheringMembers(invitation2.id, listOf(member2.id))

        // then
        assertThat(result1.invitationMemberIds).hasSize(1)
        assertThat(result2.invitationMemberIds).hasSize(1)
        
        val savedInvitationMembers = invitationMemberRepository.findAll()
        assertThat(savedInvitationMembers).hasSize(2)

        // 각 초대장에 올바른 멤버가 연결되었는지 확인
        val invitation1Members = savedInvitationMembers.filter { it.invitation.id == invitation1.id }
        val invitation2Members = savedInvitationMembers.filter { it.invitation.id == invitation2.id }
        
        assertThat(invitation1Members).hasSize(1)
        assertThat(invitation1Members.first().memberId).isEqualTo(member1.id)
        
        assertThat(invitation2Members).hasSize(1)
        assertThat(invitation2Members.first().memberId).isEqualTo(member2.id)
    }
}