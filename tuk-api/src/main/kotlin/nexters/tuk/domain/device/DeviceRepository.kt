package nexters.tuk.domain.device

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceRepository : JpaRepository<Device, Long> {
    fun findByDeviceIdAndMemberId(deviceId: String, memberId: Long): Device?
    fun findByMemberIdIn(memberIds: List<Long>): List<Device>
}