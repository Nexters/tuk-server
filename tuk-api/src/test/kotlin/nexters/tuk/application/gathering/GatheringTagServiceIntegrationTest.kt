package nexters.tuk.application.gathering

import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.GatheringTagRepository
import nexters.tuk.domain.member.MemberRepository
import nexters.tuk.domain.tag.Category
import nexters.tuk.domain.tag.CategoryRepository
import nexters.tuk.domain.tag.Tag
import nexters.tuk.domain.tag.TagRepository
import nexters.tuk.fixtures.GatheringFixtureHelper
import nexters.tuk.fixtures.MemberFixtureHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GatheringTagServiceIntegrationTest @Autowired constructor(
    private val gatheringTagService: GatheringTagService,
    private val gatheringRepository: GatheringRepository,
    private val gatheringTagRepository: GatheringTagRepository,
    private val gatheringMemberRepository: GatheringMemberRepository,
    private val memberRepository: MemberRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
) {

    private val memberFixture = MemberFixtureHelper(memberRepository)
    private val gatheringFixture = GatheringFixtureHelper(gatheringRepository, gatheringMemberRepository)
    private lateinit var category: Category
    private lateinit var tag1: Tag
    private lateinit var tag2: Tag
    private lateinit var tag3: Tag

    @BeforeEach
    fun setUp() {
        category = categoryRepository.save(Category("테스트 카테고리"))
        tag1 = tagRepository.save(Tag("태그1", category))
        tag2 = tagRepository.save(Tag("태그2", category))
        tag3 = tagRepository.save(Tag("태그3", category))
    }

    @AfterEach
    fun tearDown() {
        gatheringTagRepository.deleteAllInBatch()
        gatheringRepository.deleteAllInBatch()
        tagRepository.deleteAllInBatch()
        categoryRepository.deleteAllInBatch()
        memberRepository.deleteAllInBatch()
    }

    @Test
    fun `모임에 태그를 성공적으로 추가한다`() {
        // given
        val member = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(member, "테스트 모임")
        val tagIds = listOf(tag1.id, tag2.id)

        // when
        val result = gatheringTagService.addTags(gathering.id, tagIds)

        // then
        assertThat(result.ids).hasSize(2)

        val savedGatheringTags = gatheringTagRepository.findAllByGathering(gathering)
        assertThat(savedGatheringTags).hasSize(2)
        assertThat(savedGatheringTags.map { it.tagId }).containsExactlyInAnyOrder(tag1.id, tag2.id)
    }

    @Test
    fun `빈 태그 목록으로 호출해도 정상 처리된다`() {
        // given
        val member = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(member, "테스트 모임")
        val emptyTagIds = emptyList<Long>()

        // when
        val result = gatheringTagService.addTags(gathering.id, emptyTagIds)

        // then
        assertThat(result.ids).isEmpty()

        val savedGatheringTags = gatheringTagRepository.findAllByGathering(gathering)
        assertThat(savedGatheringTags).isEmpty()
    }

    @Test
    fun `단일 태그를 추가할 수 있다`() {
        // given
        val member = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(member, "테스트 모임")
        val tagIds = listOf(tag1.id)

        // when
        val result = gatheringTagService.addTags(gathering.id, tagIds)

        // then
        assertThat(result.ids).hasSize(1)

        val savedGatheringTags = gatheringTagRepository.findAllByGathering(gathering)
        assertThat(savedGatheringTags).hasSize(1)
        assertThat(savedGatheringTags.first().tagId).isEqualTo(tag1.id)
    }

    @Test
    fun `여러 태그를 한번에 추가할 수 있다`() {
        // given
        val member = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(member, "테스트 모임")
        val tagIds = listOf(tag1.id, tag2.id, tag3.id)

        // when
        val result = gatheringTagService.addTags(gathering.id, tagIds)

        // then
        assertThat(result.ids).hasSize(3)

        val savedGatheringTags = gatheringTagRepository.findAllByGathering(gathering)
        assertThat(savedGatheringTags).hasSize(3)
        assertThat(savedGatheringTags.map { it.tagId }).containsExactlyInAnyOrder(tag1.id, tag2.id, tag3.id)
    }

    @Test
    fun `중복된 태그 ID가 있어도 정상 처리된다`() {
        // given
        val member = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(member, "테스트 모임")
        val duplicateTagIds = listOf(tag1.id, tag1.id, tag2.id, tag2.id)

        // when
        val result = gatheringTagService.addTags(gathering.id, duplicateTagIds)

        // then
        assertThat(result.ids).hasSize(4) // 중복이 포함된 결과

        val savedGatheringTags = gatheringTagRepository.findAllByGathering(gathering)
        // DB 제약조건에 따라 실제 저장된 수는 다를 수 있음
        assertThat(savedGatheringTags.map { it.tagId }).contains(tag1.id, tag2.id)
    }

    @Test
    fun `서로 다른 모임에 같은 태그를 추가할 수 있다`() {
        // given
        val member1 = memberFixture.createMember(socialId = "1", email = "test1@test.com")
        val member2 = memberFixture.createMember(socialId = "2", email = "test2@test.com")
        val gathering1 = gatheringFixture.createGathering(member1, "모임1")
        val gathering2 = gatheringFixture.createGathering(member2, "모임2")
        val tagIds = listOf(tag1.id, tag2.id)

        // when
        val result1 = gatheringTagService.addTags(gathering1.id, tagIds)
        val result2 = gatheringTagService.addTags(gathering2.id, tagIds)

        // then
        assertThat(result1.ids).hasSize(2)
        assertThat(result2.ids).hasSize(2)

        val savedGatheringTags1 = gatheringTagRepository.findAllByGathering(gathering1)
        val savedGatheringTags2 = gatheringTagRepository.findAllByGathering(gathering2)

        assertThat(savedGatheringTags1).hasSize(2)
        assertThat(savedGatheringTags2).hasSize(2)
        assertThat(savedGatheringTags1.map { it.tagId }).containsExactlyInAnyOrder(tag1.id, tag2.id)
        assertThat(savedGatheringTags2.map { it.tagId }).containsExactlyInAnyOrder(tag1.id, tag2.id)
    }

    @Test
    fun `생성된 GatheringTag의 ID가 정상적으로 반환된다`() {
        // given
        val member = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(member, "테스트 모임")
        val tagIds = listOf(tag1.id)

        // when
        val result = gatheringTagService.addTags(gathering.id, tagIds)

        // then
        assertThat(result.ids).hasSize(1)
        assertThat(result.ids.first()).isGreaterThan(0)

        val savedGatheringTag = gatheringTagRepository.findAllByGathering(gathering).first()
        assertThat(result.ids.first()).isEqualTo(savedGatheringTag.id)
    }

    @Test
    fun `같은 모임에 추가로 태그를 더 추가할 수 있다`() {
        // given
        val member = memberFixture.createMember()
        val gathering = gatheringFixture.createGathering(member, "테스트 모임")

        // 첫 번째 태그 추가
        gatheringTagService.addTags(gathering.id, listOf(tag1.id))

        // when - 추가 태그 추가
        val result = gatheringTagService.addTags(gathering.id, listOf(tag2.id, tag3.id))

        // then
        assertThat(result.ids).hasSize(2)

        val allGatheringTags = gatheringTagRepository.findAllByGathering(gathering)
        assertThat(allGatheringTags).hasSize(3) // 총 3개의 태그
        assertThat(allGatheringTags.map { it.tagId }).containsExactlyInAnyOrder(tag1.id, tag2.id, tag3.id)
    }
}