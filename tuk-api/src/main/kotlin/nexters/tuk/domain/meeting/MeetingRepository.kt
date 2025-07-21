package nexters.tuk.domain.meeting

import org.springframework.data.jpa.repository.JpaRepository

interface MeetingRepository: JpaRepository<Meeting, Long> {
}