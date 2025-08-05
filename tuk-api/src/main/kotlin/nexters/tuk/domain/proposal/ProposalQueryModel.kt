package nexters.tuk.domain.proposal

import java.time.LocalDateTime

class ProposalQueryModel {
    data class ProposalDetail(
        val id: Long,
        val gatheringName: String,
        val purpose: String,
        val isRead: Boolean,
        val createdAt: LocalDateTime,
    )
}