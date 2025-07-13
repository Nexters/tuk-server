package nexters.tuk.domain

import jakarta.persistence.*
import java.time.ZonedDateTime

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: ZonedDateTime
        private set

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: ZonedDateTime
        private set

    @Column(name = "deleted_at")
    var deletedAt: ZonedDateTime? = null
        private set
}
