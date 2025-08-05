package nexters.tuk.domain.purpose

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import nexters.tuk.domain.BaseEntity

@Entity
class Purpose(
    @Enumerated(EnumType.STRING)
    val type: PurposeType,

    @Column(name = "tag")
    val tag: String
) : BaseEntity()

enum class PurposeType {
    WHERE_TAG, WHEN_TAG, WHAT_TAG
}