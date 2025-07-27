package nexters.tuk.domain.gathering

import jakarta.persistence.*

@Entity
class GatheringTag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false, updatable = false)
    val gathering: Gathering,

    @Column(name = "tag_id", nullable = false, updatable = false)
    val tagId: Long,
) {
    companion object {
        fun addTag(gathering: Gathering, tagId: Long): GatheringTag {
            return GatheringTag(0, gathering, tagId)
        }
    }
}