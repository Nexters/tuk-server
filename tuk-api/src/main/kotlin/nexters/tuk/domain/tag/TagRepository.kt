package nexters.tuk.domain.tag

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query


interface TagRepository : JpaRepository<Tag, Long> {
    @Query("""
        SELECT tag 
        FROM Tag tag
        JOIN FETCH tag.category
    """)
    fun findAllWithCategory() : List<Tag>
}