package nexters.tuk.domain.gathering

import java.time.LocalDateTime

class Gathering(
    val id: Long,
    val intervalDays: Long,
    val memberIds: List<Long>,
    val createdAt: LocalDateTime,
    val lastPushedAt: LocalDateTime?,
)