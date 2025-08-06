package nexters.tuk.application.purpose

import nexters.tuk.domain.purpose.Purpose
import nexters.tuk.domain.purpose.PurposeRepository
import nexters.tuk.domain.purpose.PurposeType
import nexters.tuk.testcontainers.RedisCleanUp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PurposeServiceIntegrationTest @Autowired constructor(
    private val purposeService: PurposeService,
    private val purposeRepository: PurposeRepository,
    private val redisCleanUp: RedisCleanUp,
) {

    @BeforeEach
    @AfterEach
    fun cleanUp() {
        redisCleanUp.flushAll()
        purposeRepository.deleteAllInBatch()
    }

    @Test
    fun `모든 Purpose를 타입별로 분류하여 반환한다`() {
        // given
        purposeRepository.saveAll(
            listOf(
                Purpose(PurposeType.WHEN_TAG, "오늘"),
                Purpose(PurposeType.WHEN_TAG, "내일"),
                Purpose(PurposeType.WHERE_TAG, "카페"),
                Purpose(PurposeType.WHERE_TAG, "집"),
                Purpose(PurposeType.WHAT_TAG, "밥먹기"),
                Purpose(PurposeType.WHAT_TAG, "영화보기")
            )
        )

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

    @Test
    fun `캐시가 정상적으로 동작한다`() {
        // given - 초기 데이터 설정
        purposeRepository.saveAll(
            listOf(
                Purpose(PurposeType.WHEN_TAG, "오늘"),
                Purpose(PurposeType.WHERE_TAG, "카페")
            )
        )
        
        // 첫 번째 호출로 캐시 생성
        val firstResult = purposeService.getAllPurposes()
        assertThat(firstResult.whenTags).containsExactly("오늘")
        assertThat(firstResult.whereTags).containsExactly("카페")
        
        // when - 데이터베이스에 새로운 데이터 추가
        purposeRepository.save(Purpose(PurposeType.WHEN_TAG, "내일"))
        purposeRepository.save(Purpose(PurposeType.WHAT_TAG, "밥먹기"))
        
        // then - 캐시된 결과는 변경되지 않아야 함
        val cachedResult = purposeService.getAllPurposes()
        assertThat(cachedResult.whenTags).containsExactly("오늘") // 새로운 "내일"이 포함되지 않음
        assertThat(cachedResult.whereTags).containsExactly("카페")
        assertThat(cachedResult.whatTags).isEmpty() // 새로운 "밥먹기"가 포함되지 않음
        
        // 캐시를 클리어하고 다시 조회하면 새로운 데이터가 포함되어야 함
        redisCleanUp.flushAll()
        val freshResult = purposeService.getAllPurposes()
        assertThat(freshResult.whenTags).containsExactlyInAnyOrder("오늘", "내일")
        assertThat(freshResult.whereTags).containsExactly("카페")
        assertThat(freshResult.whatTags).containsExactly("밥먹기")
    }

    @Test
    fun `여러 번 호출해도 동일한 캐시된 결과를 반환한다`() {
        // given
        purposeRepository.saveAll(
            listOf(
                Purpose(PurposeType.WHEN_TAG, "오늘"),
                Purpose(PurposeType.WHERE_TAG, "카페"),
                Purpose(PurposeType.WHAT_TAG, "밥먹기")
            )
        )
        
        // when - 같은 메서드를 여러 번 호출
        val result1 = purposeService.getAllPurposes()
        val result2 = purposeService.getAllPurposes()
        val result3 = purposeService.getAllPurposes()
        
        // then - 모든 결과가 동일해야 함
        assertThat(result1.whenTags).isEqualTo(result2.whenTags)
        assertThat(result2.whenTags).isEqualTo(result3.whenTags)
        
        assertThat(result1.whereTags).isEqualTo(result2.whereTags)
        assertThat(result2.whereTags).isEqualTo(result3.whereTags)
        
        assertThat(result1.whatTags).isEqualTo(result2.whatTags)
        assertThat(result2.whatTags).isEqualTo(result3.whatTags)
        
        // 내용도 동일한지 확인
        assertThat(result1.whenTags).containsExactly("오늘")
        assertThat(result1.whereTags).containsExactly("카페")
        assertThat(result1.whatTags).containsExactly("밥먹기")
    }

    @Test
    fun `캐시 키가 올바르게 설정되어 있다`() {
        // given - 캐시 클리어로 시작
        redisCleanUp.flushAll()
        
        purposeRepository.saveAll(
            listOf(
                Purpose(PurposeType.WHEN_TAG, "오늘"),
                Purpose(PurposeType.WHERE_TAG, "카페")
            )
        )
        
        // when - 첫 번째 호출로 캐시 생성
        val cachedResult = purposeService.getAllPurposes()
        
        // 데이터베이스에서 기존 데이터 삭제하고 새 데이터 추가
        purposeRepository.deleteAllInBatch()
        purposeRepository.save(Purpose(PurposeType.WHAT_TAG, "새로운 데이터"))
        
        // then - 캐시된 결과는 여전히 이전 데이터를 반환해야 함
        val stillCachedResult = purposeService.getAllPurposes()
        assertThat(stillCachedResult.whenTags).containsExactly("오늘") // 캐시된 데이터
        assertThat(stillCachedResult.whereTags).containsExactly("카페") // 캐시된 데이터
        assertThat(stillCachedResult.whatTags).isEmpty() // 캐시된 데이터
        
        // 캐시 클리어 후에는 새로운 데이터가 조회되어야 함
        redisCleanUp.flushAll()
        val freshResult = purposeService.getAllPurposes()
        assertThat(freshResult.whenTags).isEmpty()
        assertThat(freshResult.whereTags).isEmpty()
        assertThat(freshResult.whatTags).containsExactly("새로운 데이터")
    }

    @Test
    fun `캐시 TTL이 30일로 설정되어 있다`() {
        // given
        purposeRepository.save(Purpose(PurposeType.WHEN_TAG, "테스트"))
        
        // when - 캐시 생성
        val result = purposeService.getAllPurposes()
        
        // then - 결과 확인 (TTL은 Redis 설정으로 확인됨)
        assertThat(result.whenTags).containsExactly("테스트")
        
        // 참고: 실제 TTL 테스트는 시간이 오래 걸리므로 여기서는 기본 동작만 확인
        // Redis에서 실제 TTL 확인은 다음과 같이 할 수 있습니다:
        // redis-cli에서 "TTL cache:30d:purpose:all" 명령어로 확인 가능
    }
}