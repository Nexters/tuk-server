package nexters.tuk.domain.gathering

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import nexters.tuk.domain.BaseEntity
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@SQLRestriction("deleted_at is NULL")
@Entity
@Table(name = "gathering")
class Gathering private constructor(
    @Column(name = "gathering_name", nullable = false)
    val name: String,

    @Column(name = "interval_days", nullable = false)
    val intervalDays: Long,

    @Column(name = "member_id", nullable = false, updatable = false)
    val hostId: Long,

    lastPushedAt: LocalDateTime?,
) : BaseEntity() {
    @Column(name = "last_pushed_at", nullable = true, updatable = false)
    var lastPushedAt = lastPushedAt
        private set

    fun updatePushStatus() {
        this.lastPushedAt = LocalDateTime.now()
    }

    fun isHost(memberId: Long): Boolean {
        return hostId == memberId
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

