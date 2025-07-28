package nexters.tuk.domain.tag

import jakarta.persistence.Column
import jakarta.persistence.Entity
import nexters.tuk.domain.BaseEntity

@Entity
class Category(
    @Column(name = "name", nullable = false)
    val name: String,
) : BaseEntity() {
}