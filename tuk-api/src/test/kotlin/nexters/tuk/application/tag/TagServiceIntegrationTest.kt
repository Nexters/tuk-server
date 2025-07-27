package nexters.tuk.application.tag

import nexters.tuk.domain.tag.Category
import nexters.tuk.domain.tag.CategoryRepository
import nexters.tuk.domain.tag.Tag
import nexters.tuk.domain.tag.TagRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TagServiceIntegrationTest @Autowired constructor(
    private val tagService: TagService,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
) {

    private lateinit var category1: Category
    private lateinit var category2: Category
    private lateinit var tag1: Tag
    private lateinit var tag2: Tag
    private lateinit var tag3: Tag
    private lateinit var tag4: Tag

    @BeforeEach
    fun setUp() {
        category1 = categoryRepository.save(Category("카테고리1"))
        category2 = categoryRepository.save(Category("카테고리2"))
        
        tag1 = tagRepository.save(Tag("태그1", category1))
        tag2 = tagRepository.save(Tag("태그2", category1))
        tag3 = tagRepository.save(Tag("태그3", category2))
        tag4 = tagRepository.save(Tag("태그4", category2))
    }

    @AfterEach
    fun tearDown() {
        tagRepository.deleteAllInBatch()
        categoryRepository.deleteAllInBatch()
    }

    @Test
    fun `카테고리별로 태그를 정상적으로 조회한다`() {
        // when
        val result = tagService.getCategorizedTags()

        // then
        assertThat(result.categories).hasSize(2)
        
        val category1Group = result.categories.find { it.categoryName == "카테고리1" }
        val category2Group = result.categories.find { it.categoryName == "카테고리2" }
        
        assertThat(category1Group).isNotNull
        assertThat(category2Group).isNotNull
        
        assertThat(category1Group!!.tags).hasSize(2)
        assertThat(category2Group!!.tags).hasSize(2)
        
        assertThat(category1Group.tags.map { it.name }).containsExactlyInAnyOrder("태그1", "태그2")
        assertThat(category2Group.tags.map { it.name }).containsExactlyInAnyOrder("태그3", "태그4")
    }

    @Test
    fun `태그가 없는 카테고리는 조회되지 않는다`() {
        // given
        val emptyCategory = categoryRepository.save(Category("빈 카테고리"))

        // when
        val result = tagService.getCategorizedTags()

        // then
        assertThat(result.categories).hasSize(2) // 기존 2개 카테고리만
        assertThat(result.categories.map { it.categoryName }).doesNotContain("빈 카테고리")
    }

    @Test
    fun `단일 카테고리에 단일 태그만 있어도 정상 조회된다`() {
        // given
        tagRepository.deleteAllInBatch() // 기존 데이터 삭제
        
        val singleCategory = categoryRepository.save(Category("단일 카테고리"))
        val singleTag = tagRepository.save(Tag("단일 태그", singleCategory))

        // when
        val result = tagService.getCategorizedTags()

        // then
        assertThat(result.categories).hasSize(1)
        assertThat(result.categories.first().categoryName).isEqualTo("단일 카테고리")
        assertThat(result.categories.first().tags).hasSize(1)
        assertThat(result.categories.first().tags.first().name).isEqualTo("단일 태그")
    }

    @Test
    fun `카테고리에 많은 태그가 있어도 정상 조회된다`() {
        // given
        val manyTagsCategory = categoryRepository.save(Category("많은 태그 카테고리"))
        val manyTags = (1..20).map { 
            tagRepository.save(Tag("태그$it", manyTagsCategory)) 
        }

        // when
        val result = tagService.getCategorizedTags()

        // then
        val manyTagsGroup = result.categories.find { it.categoryName == "많은 태그 카테고리" }
        assertThat(manyTagsGroup).isNotNull
        assertThat(manyTagsGroup!!.tags).hasSize(20)
        
        val expectedTagNames = (1..20).map { "태그$it" }
        assertThat(manyTagsGroup.tags.map { it.name }).containsExactlyInAnyOrderElementsOf(expectedTagNames)
    }

    @Test
    fun `태그 ID가 정상적으로 반환된다`() {
        // when
        val result = tagService.getCategorizedTags()

        // then
        val category1Group = result.categories.find { it.categoryName == "카테고리1" }!!
        val tag1Item = category1Group.tags.find { it.name == "태그1" }!!
        val tag2Item = category1Group.tags.find { it.name == "태그2" }!!
        
        assertThat(tag1Item.id).isEqualTo(tag1.id)
        assertThat(tag2Item.id).isEqualTo(tag2.id)
    }

    @Test
    fun `태그와 카테고리 데이터가 모두 없어도 빈 결과를 반환한다`() {
        // given
        tagRepository.deleteAllInBatch() // 모든 데이터 삭제

        // when
        val result = tagService.getCategorizedTags()

        // then
        assertThat(result.categories).isEmpty()
    }

    @Test
    fun `같은 이름의 카테고리가 여러 개 있어도 정상 처리된다`() {
        // given
        val duplicateCategory1 = categoryRepository.save(Category("중복 카테고리"))
        val duplicateCategory2 = categoryRepository.save(Category("중복 카테고리"))
        
        val tagA = tagRepository.save(Tag("태그A", duplicateCategory1))
        val tagB = tagRepository.save(Tag("태그B", duplicateCategory2))

        // when
        val result = tagService.getCategorizedTags()

        // then
        val duplicateGroups = result.categories.filter { it.categoryName == "중복 카테고리" }
        assertThat(duplicateGroups).hasSize(2)
        
        val allTagNames = duplicateGroups.flatMap { it.tags.map { tag -> tag.name } }
        assertThat(allTagNames).containsExactlyInAnyOrder("태그A", "태그B")
    }

    @Test
    fun `특수문자가 포함된 카테고리와 태그 이름도 정상 조회된다`() {
        // given
        val specialCategory = categoryRepository.save(Category("특수!@#카테고리"))
        val specialTag = tagRepository.save(Tag("특수$%^태그", specialCategory))

        // when
        val result = tagService.getCategorizedTags()

        // then
        val specialGroup = result.categories.find { it.categoryName == "특수!@#카테고리" }
        assertThat(specialGroup).isNotNull
        assertThat(specialGroup!!.tags).hasSize(1)
        assertThat(specialGroup.tags.first().name).isEqualTo("특수$%^태그")
    }

    @Test
    fun `긴 이름의 카테고리와 태그도 정상 조회된다`() {
        // given
        val longCategoryName = "이것은 매우 긴 카테고리 이름입니다. 실제로 사용자가 입력할 수 있는 범위의 긴 이름을 테스트합니다."
        val longTagName = "이것은 매우 긴 태그 이름입니다. 실제로 사용자가 입력할 수 있는 범위의 긴 이름을 테스트합니다."
        
        val longCategory = categoryRepository.save(Category(longCategoryName))
        val longTag = tagRepository.save(Tag(longTagName, longCategory))

        // when
        val result = tagService.getCategorizedTags()

        // then
        val longGroup = result.categories.find { it.categoryName == longCategoryName }
        assertThat(longGroup).isNotNull
        assertThat(longGroup!!.tags).hasSize(1)
        assertThat(longGroup.tags.first().name).isEqualTo(longTagName)
    }
}