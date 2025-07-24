package nexters.tuk.domain.gathering

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.domain.BaseEntity
import nexters.tuk.infrastructure.jpa.StringSetConverter
import java.time.LocalDate

@Entity
@Table(name = "gathering")
class Gathering private constructor(
    @Column(name = "gathering_name", nullable = false)
    val name: String,

    @Column(name = "first_gathering_date", nullable = false, updatable = false)
    val firstGatheringDate: LocalDate,

    @Column(name = "last_gathering_date", nullable = false, updatable = false)
    val lastGatheringDate: LocalDate,

    @Column(name = "interval_days", nullable = false)
    val intervalDays: Long,

    @Column(name = "member_id", nullable = false, updatable = false)
    val hostId: Long,

    @Convert(converter = StringSetConverter::class)
    @Column(name = "tags", columnDefinition = "json")
    val tags: Set<String> = setOf()

) : BaseEntity() {
    companion object {
        fun generate(command: GatheringCommand.Generate): Gathering {
            return Gathering(
                hostId = command.memberId,
                name = command.gatheringName,
                firstGatheringDate = LocalDate.now().minusDays(command.daysSinceLastGathering),
                lastGatheringDate = LocalDate.now().minusDays(command.daysSinceLastGathering),
                intervalDays = command.gatheringIntervalDays,
                tags = command.tags.toSet(),
            )
        }
    }

    fun isHost(memberId: Long): Boolean {
        return hostId == memberId
    }
}

