package nexters.tuk.fixtures

import nexters.tuk.application.gathering.dto.request.GatheringCommand
import nexters.tuk.domain.gathering.Gathering
import nexters.tuk.domain.gathering.GatheringMember
import nexters.tuk.domain.gathering.GatheringMemberRepository
import nexters.tuk.domain.gathering.GatheringRepository
import nexters.tuk.domain.member.Member

object GatheringFixture {
    fun gatheringGenerateCommand(
        memberId: Long,
        gatheringName: String = "test gathering",
        gatheringIntervalDays: Long = 7,
        tags: List<Long> = emptyList()
    ) = GatheringCommand.Generate(
        memberId = memberId,
        gatheringName = gatheringName,
        gatheringIntervalDays = gatheringIntervalDays,
        tags = tags
    )
}

class GatheringFixtureHelper(
    private val gatheringRepository: GatheringRepository,
    private val gatheringMemberRepository: GatheringMemberRepository
) {
    fun createGathering(
        hostMember: Member,
        name: String = "test gathering",
        intervalDays: Long = 7,
        tags: List<Long> = emptyList()
    ): Gathering = gatheringRepository.save(
        Gathering.generate(
            hostId = hostMember.id,
            name = name,
            intervalDays = intervalDays
        )
    )

    fun registerGatheringMember(gathering: Gathering, member: Member): GatheringMember =
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))
}