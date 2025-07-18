package nexters.tuk.application.member

import org.springframework.stereotype.Service

@Service
class MemberServiceImpl : MemberService {
    override fun findTokensByMeetingId(meetingId: Long): List<String> {
        TODO("Not yet implemented")
    }
}