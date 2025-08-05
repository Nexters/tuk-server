package nexters.tuk.application.purpose

import nexters.tuk.domain.purpose.Purpose
import nexters.tuk.domain.purpose.PurposeRepository
import nexters.tuk.domain.purpose.PurposeType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager

@SpringBootTest
class PurposeServiceIntegrationTest @Autowired constructor(
    private val purposeService: PurposeService,
    private val purposeRepository: PurposeRepository,
    private val cacheManager: CacheManager,
) {

    @BeforeEach
    @AfterEach
    fun cleanUp() {
        cacheManager.getCache("purposes")?.clear()
        purposeRepository.deleteAllInBatch()
    }

    @Test
    fun `모든 Purpose를 타입별로 분류하여 반환한다`() {
        // given
        purposeRepository.saveAll(listOf(
            Purpose(PurposeType.WHEN_TAG, "오늘"),
            Purpose(PurposeType.WHEN_TAG, "내일"),
            Purpose(PurposeType.WHERE_TAG, "카페"),
            Purpose(PurposeType.WHERE_TAG, "집"),
            Purpose(PurposeType.WHAT_TAG, "밥먹기"),
            Purpose(PurposeType.WHAT_TAG, "영화보기")
        ))

        // when
        val result = purposeService.getAllPurposes()

        // then
        assertThat(result.whenTags).containsExactlyInAnyOrder("오늘", "내일")
        assertThat(result.whereTags).containsExactlyInAnyOrder("카페", "집")
        assertThat(result.whatTags).containsExactlyInAnyOrder("밥먹기", "영화보기")
    }

    @Test
    fun `특정 타입의 Purpose가 없으면 빈 리스트를 반환한다`() {
        // given
        purposeRepository.save(Purpose(PurposeType.WHEN_TAG, "오늘"))

        // when
        val result = purposeService.getAllPurposes()

        // then
        assertThat(result.whenTags).containsExactly("오늘")
        assertThat(result.whereTags).isEmpty()
        assertThat(result.whatTags).isEmpty()
    }

    @Test
    fun `Purpose가 없으면 모든 리스트가 비어있다`() {
        // when
        val result = purposeService.getAllPurposes()

        // then
        assertThat(result.whenTags).isEmpty()
        assertThat(result.whereTags).isEmpty()
        assertThat(result.whatTags).isEmpty()
    }
}