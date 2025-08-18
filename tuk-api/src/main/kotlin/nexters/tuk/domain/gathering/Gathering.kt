package nexters.tuk.domain.gathering

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.domain.BaseEntity
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@SQLRestriction("deleted_at is NULL")
@Entity
@Table(name = "gathering")
class Gathering private constructor(
    @Column(name = "gathering_name", nullable = false)
    val name: String,

    intervalDays: Long,

    @Column(name = "member_id", nullable = false, updatable = false)
    val hostId: Long,

    lastPushedAt: LocalDateTime?,
) : BaseEntity() {
    @Column(name = "last_pushed_at", nullable = true)
    var lastPushedAt = lastPushedAt
        private set

    @Column(name = "interval_days", nullable = false)
    var intervalDays: Long = intervalDays
        private set

    fun updatePushStatus() {
        this.lastPushedAt = LocalDateTime.now()
    }

    fun isHost(memberId: Long): Boolean {
        return hostId == memberId
    }

    fun update(command: GatheringCommand.Update) {
        this.intervalDays = command.gatheringIntervalDays
    }

    companion object {
        fun generate(hostId: Long, name: String, intervalDays: Long): Gathering {
            return Gathering(
                hostId = hostId,
                name = name,
                intervalDays = intervalDays,
                lastPushedAt = null
            )
        }
    }
}

