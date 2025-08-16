package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.contract.BaseException
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.jdbc.Sql

@Sql("/truncate.sql")
@SpringBootTest
class GatheringCommandServiceIntegrationTest @Autowired constructor(
    private val gatheringCommandService: GatheringCommandService,
    private val gatheringRepository: GatheringRepository,
) {
    @Test
    fun `모임 주기를 변경 가능하다`() {
        // given
        val gathering = gatheringRepository.save(
            Gathering.generate(
                hostId = 1L,
                name = "모임",
                intervalDays = 10
            )
        )

        // when
        val actual = gatheringCommandService.updateGathering(
            GatheringCommand.Update(
                memberId = 1L,
                gatheringId = gathering.id,
                gatheringIntervalDays = 30
            )
        )

        // then
        assertAll(
            { assertThat(actual).isNotNull },
            {
                assertThat(gatheringRepository.findByIdOrThrow(gatheringId = gathering.id).intervalDays)
                    .isEqualTo(30)
            },
        )
    }

    @Test
    fun `호스트가 아닌 사용자는 모임을 수정할 수 없다`() {
        // given
        val gathering = gatheringRepository.save(
            Gathering.generate(
                hostId = 1L,
                name = "모임",
                intervalDays = 10
            )
        )

        // when & then
        val exception = assertThrows<BaseException> {
            gatheringCommandService.updateGathering(
                GatheringCommand.Update(
                    memberId = 2L, // 호스트가 아닌 다른 사용자
                    gatheringId = gathering.id,
                    gatheringIntervalDays = 30
                )
            )
        }

        assertThat(exception.message).isEqualTo("수정 권한이 없습니다.")
    }

    @Test
    fun `호스트가 모임을 삭제할 수 있다`() {
        // given
        val gathering = gatheringRepository.save(
            Gathering.generate(
                hostId = 1L,
                name = "모임",
                intervalDays = 10
            )
        )

        // when
        gatheringCommandService.deleteGathering(
            GatheringCommand.Delete(
                memberId = 1L,
                gatheringId = gathering.id
            )
        )

        // then
        val deletedGathering = gatheringRepository.findByIdOrNull(gathering.id)
        assertNull(deletedGathering)
    }

    @Test
    fun `호스트가 아닌 사용자는 모임을 삭제할 수 없다`() {
        // given
        val gathering = gatheringRepository.save(
            Gathering.generate(
                hostId = 1L,
                name = "모임",
                intervalDays = 10
            )
        )

        // when & then
        val exception = assertThrows<BaseException> {
            gatheringCommandService.deleteGathering(
                GatheringCommand.Delete(
                    memberId = 2L, // 호스트가 아닌 다른 사용자
                    gatheringId = gathering.id
                )
            )
        }

        assertThat(exception.message).isEqualTo("수정 권한이 없습니다.")
    }
}