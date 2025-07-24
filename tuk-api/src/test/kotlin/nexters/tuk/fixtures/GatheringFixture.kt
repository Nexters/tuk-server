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
        daysSinceLastGathering: Long = 0,
        gatheringIntervalDays: Long = 7,
        tags: List<String> = emptyList()
    ) = GatheringCommand.Generate(
        memberId = memberId,
        gatheringName = gatheringName,
        daysSinceLastGathering = daysSinceLastGathering,
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
        daysSince: Long = 0,
        intervalDays: Long = 7,
        tags: List<String> = emptyList()
    ): Gathering = gatheringRepository.save(
        Gathering.generate(
            GatheringFixture.gatheringGenerateCommand(
                memberId = hostMember.id,
                gatheringName = name,
                daysSinceLastGathering = daysSince,
                gatheringIntervalDays = intervalDays,
                tags = tags
            )
        )
    )

    fun registerGatheringMember(gathering: Gathering, member: Member): GatheringMember =
        gatheringMemberRepository.save(GatheringMember.registerMember(gathering, member.id))
}