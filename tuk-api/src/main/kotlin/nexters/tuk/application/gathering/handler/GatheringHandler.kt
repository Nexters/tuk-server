package nexters.tuk.application.gathering.handler

import nexters.tuk.application.gathering.GatheringMemberService
import nexters.tuk.application.member.MemberService
import nexters.tuk.application.member.event.MemberEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class GatheringHandler(
    private val gatheringMemberService: GatheringMemberService,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleDeleteMember(event: MemberEvent.MemberDeleted) {
        gatheringMemberService.deleteGatheringMember(event.memberId)
    }
}