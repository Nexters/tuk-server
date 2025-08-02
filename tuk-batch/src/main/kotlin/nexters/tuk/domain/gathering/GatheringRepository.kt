package nexters.tuk.domain.gathering

interface GatheringRepository {
    fun getAllGatheringsWithMember(): List<GatheringMembers>
    fun findById(id: Long): Gathering?
}