package nexters.tuk.domain.gathering

interface GatheringRepository {
    fun getAllGatherings(): List<Gathering>
}