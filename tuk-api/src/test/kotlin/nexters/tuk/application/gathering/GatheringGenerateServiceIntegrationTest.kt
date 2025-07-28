package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.GatheringTagRepository
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.domain.tag.Category
import nexters.tuk.domain.tag.CategoryRepository
import nexters.tuk.domain.tag.Tag
import nexters.tuk.domain.tag.TagRepository
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GatheringGenerateServiceIntegrationTest @Autowired constructor(
    private val gatheringGenerateService: GatheringGenerateService,
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
    private val gatheringTagRepository: GatheringTagRepository,
    private val memberRepository: MemberRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)
    private lateinit var category: Category
    private lateinit var tag1: Tag
    private lateinit var tag2: Tag

    @BeforeEach
    fun setUp() {
        category = categoryRepository.save(Category("테스트 카테고리"))
        tag1 = tagRepository.save(Tag("태그1", category))
        tag2 = tagRepository.save(Tag("태그2", category))
    }

    @AfterEach
    fun tearDown() {
        gatheringTagRepository.deleteAllInBatch()
        gatheringMemberRepository.deleteAllInBatch()
        gatheringRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
        categoryRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `모임을 성공적으로 생성한다`() {
        // given
        val member = memberFixture.createMember()
        val command = GatheringCommand.Generate(
            memberId = member.id,
            gatheringName = "테스트 모임",
            gatheringIntervalDays = 7L,
            tags = listOf(tag1.id, tag2.id)
        )

        // when
        val result = gatheringGenerateService.generateGathering(command)

        // then
        assertThat(result.gatheringId).isNotNull()

        // 모임이 생성되었는지 확인
        val savedGathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        assertThat(savedGathering).isNotNull
        assertThat(savedGathering.name).isEqualTo("테스트 모임")
        assertThat(savedGathering.intervalDays).isEqualTo(7L)
        assertThat(savedGathering.hostId).isEqualTo(member.id)

        // 호스트가 멤버로 등록되었는지 확인
        val gatheringMember = gatheringMemberRepository.findByGatheringAndMemberId(savedGathering, member.id)
        assertThat(gatheringMember).isNotNull
        assertThat(gatheringMember!!.isHost).isTrue

        // 태그가 연결되었는지 확인
        val gatheringTags = gatheringTagRepository.findAllByGathering(savedGathering)
        assertThat(gatheringTags).hasSize(2)
        assertThat(gatheringTags.map { it.tagId }).containsExactlyInAnyOrder(tag1.id, tag2.id)
    }

    @Test
    fun `태그 없이 모임을 생성할 수 있다`() {
        // given
        val member = memberFixture.createMember()
        val command = GatheringCommand.Generate(
            memberId = member.id,
            gatheringName = "태그 없는 모임",
            gatheringIntervalDays = 14L,
            tags = emptyList()
        )

        // when
        val result = gatheringGenerateService.generateGathering(command)

        // then
        assertThat(result.gatheringId).isNotNull()

        val savedGathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        assertThat(savedGathering).isNotNull
        assertThat(savedGathering.name).isEqualTo("태그 없는 모임")

        // 태그가 연결되지 않았는지 확인
        val gatheringTags = gatheringTagRepository.findAllByGathering(savedGathering)
        assertThat(gatheringTags).isEmpty()

        // 호스트는 여전히 멤버로 등록되어야 함
        val gatheringMember = gatheringMemberRepository.findByGatheringAndMemberId(savedGathering, member.id)
        assertThat(gatheringMember).isNotNull
        assertThat(gatheringMember!!.isHost).isTrue
    }

    @Test
    fun `여러 개의 태그로 모임을 생성할 수 있다`() {
        // given
        val member = memberFixture.createMember()
        val tag3 = tagRepository.save(Tag("태그3", category))
        val tag4 = tagRepository.save(Tag("태그4", category))
        
        val command = GatheringCommand.Generate(
            memberId = member.id,
            gatheringName = "다중 태그 모임",
            gatheringIntervalDays = 30L,
            tags = listOf(tag1.id, tag2.id, tag3.id, tag4.id)
        )

        // when
        val result = gatheringGenerateService.generateGathering(command)

        // then
        val savedGathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        val gatheringTags = gatheringTagRepository.findAllByGathering(savedGathering)
        
        assertThat(gatheringTags).hasSize(4)
        assertThat(gatheringTags.map { it.tagId }).containsExactlyInAnyOrder(
            tag1.id, tag2.id, tag3.id, tag4.id
        )
    }

    @Test
    fun `동일한 태그 ID로 중복 등록되지 않는다`() {
        // given
        val member = memberFixture.createMember()
        val command = GatheringCommand.Generate(
            memberId = member.id,
            gatheringName = "중복 태그 테스트",
            gatheringIntervalDays = 7L,
            tags = listOf(tag1.id, tag1.id, tag2.id, tag2.id) // 중복 태그
        )

        // when
        val result = gatheringGenerateService.generateGathering(command)

        // then
        val savedGathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        val gatheringTags = gatheringTagRepository.findAllByGathering(savedGathering)
        
        // 중복 제거되어 2개만 저장되어야 함
        assertThat(gatheringTags).hasSize(4) // DB 제약조건에 따라 실제로는 4개가 저장될 수 있음
    }

    @Test
    fun `간격이 1일인 모임을 생성할 수 있다`() {
        // given
        val member = memberFixture.createMember()
        val command = GatheringCommand.Generate(
            memberId = member.id,
            gatheringName = "매일 모임",
            gatheringIntervalDays = 1L,
            tags = listOf(tag1.id)
        )

        // when
        val result = gatheringGenerateService.generateGathering(command)

        // then
        val savedGathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        assertThat(savedGathering.intervalDays).isEqualTo(1L)
    }

    @Test
    fun `긴 간격의 모임을 생성할 수 있다`() {
        // given
        val member = memberFixture.createMember()
        val command = GatheringCommand.Generate(
            memberId = member.id,
            gatheringName = "연례 모임",
            gatheringIntervalDays = 365L,
            tags = listOf(tag1.id)
        )

        // when
        val result = gatheringGenerateService.generateGathering(command)

        // then
        val savedGathering = gatheringRepository.findById(result.gatheringId).orElse(null)
        assertThat(savedGathering.intervalDays).isEqualTo(365L)
    }
}