package nexters.tuk.domain.invitation

import org.springframework.data.jpa.repository.JpaRepository

interface InvitationMemberRepository : JpaRepository<InvitationMember, Long>
