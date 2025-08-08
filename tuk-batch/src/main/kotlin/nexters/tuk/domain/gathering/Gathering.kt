package nexters.tuk.domain.gathering

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "gathering")
class Gathering(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "gathering_name", nullable = false)
    val name: String,

    @Column(name = "interval_days", nullable = false)
    val intervalDays: Long,

    lastPushedAt: LocalDateTime?,

    @Column(name = "deleted_at", nullable = true)
    val deletedAt: LocalDateTime,
) {
    @Column(name = "last_pushed_at", nullable = true)
    var lastPushedAt = lastPushedAt
        private set

    fun updatePushStatus() {
        this.lastPushedAt = LocalDateTime.now()
    }
}

