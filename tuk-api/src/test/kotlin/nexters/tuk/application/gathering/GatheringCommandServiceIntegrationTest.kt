package nexters.tuk.application.gathering

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.gathering.findByIdOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
}