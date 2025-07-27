package nexters.tuk.domain.device

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import nexters.tuk.domain.BaseEntity
import org.hibernate.annotations.SQLRestriction

@SQLRestriction("deleted_at is NULL")
@Entity
@Table(name = "device")
class Device private constructor(
    @Column(name = "device_id", nullable = false)
    val deviceId: String,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "is_active")
    val isPushActive: Boolean,

    deviceToken: String,
    appVersion: String,
    osVersion: String,
) : BaseEntity() {

    @Column(name = "device_token", nullable = false)
    var deviceToken: String = deviceToken
        private set

    @Column(name = "app_version", length = 20)
    var appVersion: String = appVersion
        private set

    @Column(name = "os_version", length = 20)
    var osVersion: String = osVersion
        private set

    fun updateDeviceToken(
        newDeviceToken: String,
        newAppVersion: String,
        newOsVersion: String,
    ) {
        this.deviceToken = newDeviceToken
        this.appVersion = newAppVersion
        this.osVersion = newOsVersion
    }

    companion object {
        fun new(
            memberId: Long,
            deviceId: String,
            deviceToken: String,
            appVersion: String,
            osVersion: String,
        ): Device {
            return Device(
                deviceId = deviceId,
                deviceToken = deviceToken,
                memberId = memberId,
                appVersion = appVersion,
                osVersion = osVersion,
                isPushActive = true,
            )
        }
    }
}