package nexters.tuk.domain.tag

import jakarta.persistence.*
import nexters.tuk.domain.BaseEntity

@Entity
class Tag(
    @Column(name = "name")
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,
) : BaseEntity()