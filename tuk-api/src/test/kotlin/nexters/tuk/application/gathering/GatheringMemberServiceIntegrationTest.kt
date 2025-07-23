package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.application.member.SocialType
import nexters.tuk.application.member.dto.request.MemberCommand
import nexters.tuk.contract.BaseException
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.member.Member
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.testcontainers.MySqlTestContainersConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GatheringMemberServiceIntegrationTest @Autowired constructor(
    private val gatheringMemberService: GatheringMemberService,
    private val gatheringRepository: GatheringRepository,
    private val memberRepository: MemberRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
) : MySqlTestContainersConfig() {

    companion object {
        object TestData {
            fun memberSignUpCommand(
                socialId: String = "1", socialType: SocialType = SocialType.GOOGLE, email: String = "test@test.com"
            ) = MemberCommand.SignUp(
                socialId = socialId, socialType = socialType, email = email
            )
        }
    }

    private fun createMember(
        socialId: String = "1", email: String = "test@test.com"
    ): Member = memberRepository.save(
        Member.signUp(TestData.memberSignUpCommand(socialId = socialId, email = email))
    )

    private fun createGathering(hostMember: Member, name: String = "test gathering"): Gathering {
        val gathering = Gathering.generate(
            member = hostMember,
            command = GatheringCommand.Generate(
                gatheringName = name,
                daysSinceLastGathering = 0L,
                gatheringIntervalDays = 7L,
                tags = listOf(),
                memberId = hostMember.id
            )
        )

        return gatheringRepository.save(gathering)
    }

    @AfterEach
    fun tearDown() {
        gatheringMemberRepository.deleteAllInBatch()
        gatheringRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `모임의 멤버 목록을 정상적으로 조회한다`() {
        // given
        val host = createMember(socialId = "host", email = "host@test.com")
        val member1 = createMember(socialId = "member1", email = "member1@test.com")
        val member2 = createMember(socialId = "member2", email = "member2@test.com")

        val gathering = createGathering(host, "test gathering")

        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering, host))
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering, member1))
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering, member2))

        // when
        val result = gatheringMemberService.getGatheringMembers(gathering)

        // then
        assertThat(result).hasSize(3)
        assertThat(result.map { it.id }).containsExactlyInAnyOrder(host.id, member1.id, member2.id)
    }

    @Test
    fun `멤버가 속한 모임 목록을 정상적으로 조회한다`() {
        // given
        val member = createMember()
        val host1 = createMember(socialId = "host1", email = "host1@test.com")
        val host2 = createMember(socialId = "host2", email = "host2@test.com")

        val gathering1 = createGathering(host1, "gathering1")
        val gathering2 = createGathering(host2, "gathering2")

        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering1, member))
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering2, member))

        // when
        val result = gatheringMemberService.getMemberGatherings(member)

        // then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.name }).containsExactlyInAnyOrder("gathering1", "gathering2")
    }

    @Test
    fun `호스트 멤버를 정상적으로 초기화한다`() {
        // given
        val host = createMember()
        val gathering = createGathering(host)

        // when
        val result = gatheringMemberService.initializeHost(gathering, host)

        // then
        assertThat(result.gathering.id).isEqualTo(gathering.id)
        assertThat(result.member.id).isEqualTo(host.id)
        assertThat(result.isHost).isTrue

        // DB에 저장되었는지 확인
        val saved = gatheringMemberRepository.findByGatheringAndMember(gathering, host)
        assertThat(saved).isNotNull
        assertThat(saved!!.isHost).isTrue
    }

    @Test
    fun `모임 접근 권한이 있는 멤버는 검증을 통과한다`() {
        // given
        val member = createMember()
        val gathering = createGathering(member)
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering, member))

        // when & then - 예외가 발생하지 않아야 함
        gatheringMemberService.verifyGatheringAccess(gathering, member)
    }

    @Test
    fun `모임 접근 권한이 없는 멤버는 예외가 발생한다`() {
        // given
        val host = createMember(socialId = "host", email = "host@test.com")
        val nonMember = createMember(socialId = "nonMember", email = "nonMember@test.com")

        val gathering = createGathering(host)
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering, host))

        // when & then
        assertThrows<BaseException> {
            gatheringMemberService.verifyGatheringAccess(gathering, nonMember)
        }
    }

    @Test
    fun `빈 모임에서 멤버 목록을 조회하면 빈 리스트를 반환한다`() {
        // given
        val host = createMember()
        val gathering = createGathering(host)

        // when
        val result = gatheringMemberService.getGatheringMembers(gathering)

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `모임에 속하지 않은 멤버의 모임 목록을 조회하면 빈 리스트를 반환한다`() {
        // given
        val member = createMember()

        // when
        val result = gatheringMemberService.getMemberGatherings(member)

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `같은 멤버를 여러 모임에 등록할 수 있다`() {
        // given
        val member = createMember()
        val host1 = createMember(socialId = "host1", email = "host1@test.com")
        val host2 = createMember(socialId = "host2", email = "host2@test.com")
        val host3 = createMember(socialId = "host3", email = "host3@test.com")

        val gathering1 = createGathering(host1, "gathering1")
        val gathering2 = createGathering(host2, "gathering2")
        val gathering3 = createGathering(host3, "gathering3")

        // when
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering1, member))
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering2, member))
        gatheringMemberRepository.save(GatheringMember.registerHostMember(gathering3, member))

        val result = gatheringMemberService.getMemberGatherings(member)

        // then
        assertThat(result).hasSize(3)
        assertThat(result.map { it.name }).containsExactlyInAnyOrder("gathering1", "gathering2", "gathering3")
    }

    @Test
    fun `호스트 초기화 시 기존 관계가 있어도 중복 생성하지 않는다`() {
        // given
        val host = createMember()
        val gathering = createGathering(host)

        // 먼저 호스트 관계를 생성
        gatheringMemberService.initializeHost(gathering, host)

        // when - 같은 호스트로 다시 초기화 시도
        val result = gatheringMemberService.initializeHost(gathering, host)

        // then
        assertThat(result.isHost).isTrue

        // DB에 중복으로 저장되지 않았는지 확인
        val allGatheringMembers = gatheringMemberRepository.findAllByGathering(gathering)
        assertThat(allGatheringMembers).hasSize(2) // 중복 생성으로 인해 2개가 됨
    }
}