package nexters.tuk.infrastructure.mysql.gathering

import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringMembers
import nexters.tuk.domain.gathering.GatheringRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class GatheringCoreRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val gatheringJpaRepository: GatheringJpaRepository,
) : GatheringRepository {

    override fun getAllGatheringsWithMember(): List<GatheringMembers> {
        val sql = """
            SELECT g.id,
                   GROUP_CONCAT(gm.member_id) AS member_ids,
                   g.last_pushed_at,
                   g.created_at,
                   g.interval_days
            FROM gathering g
                     JOIN gathering_member gm ON g.id = gm.gathering_id
            WHERE gm.deleted_at IS NULL
              AND g.deleted_at IS NULL
            GROUP BY g.id, g.last_pushed_at, g.created_at, g.interval_days;
        """.trimIndent()

        return jdbcTemplate.query(sql) { rs, _ ->
            GatheringMembers(
                id = rs.getLong("id"),
                memberIds = rs.getString("member_ids")
                    ?.takeIf { it.isNotBlank() }
                    ?.split(",")
                    ?.map { it.trim().toLong() }
                    ?: emptyList(),
                lastPushedAt = rs.getTimestamp("last_pushed_at")?.toLocalDateTime(),
                createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
                intervalDays = rs.getLong("interval_days")
            )
        }
    }

    override fun findById(id: Long): Gathering? {
        return gatheringJpaRepository.findByIdAndDeletedAtIsNull(id)
    }
}