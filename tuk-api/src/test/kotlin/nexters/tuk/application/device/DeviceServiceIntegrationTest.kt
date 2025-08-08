package nexters.tuk.application.device

import nexters.tuk.application.device.dto.request.DeviceCommand
import nexters.tuk.domain.device.DeviceRepository
import nexters.tuk.fixtures.createDeviceInfoFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql

@Sql("/truncate.sql")
@SpringBootTest
class DeviceServiceIntegrationTest @Autowired constructor(
    private val deviceService: DeviceService,
    private val deviceRepository: DeviceRepository,
) {
    @Test
    fun `디바이스 토큰을 정상적으로 저장한다`() {
        // given
        val memberId = 1L
        val deviceInfo = createDeviceInfoFixture()

        // when
        val actual = deviceService.updateDeviceToken(
            memberId = memberId,
            command = DeviceCommand.UpdateDeviceToken(deviceInfo)
        )

        // then
        assertAll(
            { assertThat(actual.deviceToken).isEqualTo(deviceInfo.deviceToken) },
            { assertThat(actual.memberId).isEqualTo(memberId) },
        )
    }

    @Test
    fun `디바이스 토큰을 정상적으로 업데이트한다`() {
        // given
        val memberId = 1L
        val deviceInfo = createDeviceInfoFixture()
        val updateDeviceInfo = createDeviceInfoFixture(deviceToken = "updateDeviceToken")

        val firstResult = deviceService.updateDeviceToken(
            memberId = memberId,
            command = DeviceCommand.UpdateDeviceToken(deviceInfo)
        )

        val countAfterFirstInsert = deviceRepository.count()

        // when
        val actual = deviceService.updateDeviceToken(
            memberId = memberId,
            command = DeviceCommand.UpdateDeviceToken(updateDeviceInfo)
        )

        val countAfterUpdate = deviceRepository.count()
        val deviceFromDb = deviceRepository.findByDeviceIdAndMemberId(deviceInfo.deviceId, memberId)

        // then
        assertAll(
            { assertThat(actual.deviceToken).isEqualTo(updateDeviceInfo.deviceToken) },
            { assertThat(actual.memberId).isEqualTo(memberId) },
            { assertThat(actual.deviceId).isEqualTo(firstResult.deviceId) }, // 같은 디바이스 ID 확인
            { assertThat(countAfterFirstInsert).isEqualTo(1L) }, // 첫 번째 저장 후 1개
            { assertThat(countAfterUpdate).isEqualTo(1L) }, // 업데이트 후에도 여전히 1개 (새로 생성되지 않음)
            { assertThat(deviceFromDb?.deviceToken).isEqualTo(updateDeviceInfo.deviceToken) }, // DB에서 직접 확인
        )
    }

    @Test
    fun `같은 memberId와 deviceId로 여러 번 호출하면 계속 업데이트된다`() {
        // given
        val memberId = 1L
        val deviceInfo = createDeviceInfoFixture()
        
        // when & then - 첫 번째 저장
        val firstResult = deviceService.updateDeviceToken(
            memberId = memberId,
            command = DeviceCommand.UpdateDeviceToken(deviceInfo)
        )
        
        assertThat(deviceRepository.count()).isEqualTo(1L)
        assertThat(firstResult.deviceToken).isEqualTo(deviceInfo.deviceToken)

        // when & then - 두 번째 업데이트
        val updatedInfo1 = createDeviceInfoFixture(deviceToken = "token2")
        val secondResult = deviceService.updateDeviceToken(
            memberId = memberId,
            command = DeviceCommand.UpdateDeviceToken(updatedInfo1)
        )
        
        assertThat(deviceRepository.count()).isEqualTo(1L) // 여전히 1개
        assertThat(secondResult.deviceToken).isEqualTo("token2")
        assertThat(secondResult.deviceId).isEqualTo(firstResult.deviceId)

        // when & then - 세 번째 업데이트
        val updatedInfo2 = createDeviceInfoFixture(deviceToken = "token3", appVersion = "2.0")
        val thirdResult = deviceService.updateDeviceToken(
            memberId = memberId,
            command = DeviceCommand.UpdateDeviceToken(updatedInfo2)
        )
        
        assertThat(deviceRepository.count()).isEqualTo(1L) // 여전히 1개
        assertThat(thirdResult.deviceToken).isEqualTo("token3")
        assertThat(thirdResult.deviceId).isEqualTo(firstResult.deviceId)
        
        // DB에서 직접 확인
        val finalDeviceFromDb = deviceRepository.findByDeviceIdAndMemberId(deviceInfo.deviceId, memberId)
        assertThat(finalDeviceFromDb?.deviceToken).isEqualTo("token3")
    }

    @Test
    fun `다른 memberId는 별도의 디바이스로 저장된다`() {
        // given
        val memberId1 = 1L
        val memberId2 = 2L
        val deviceInfo = createDeviceInfoFixture()

        // when
        val result1 = deviceService.updateDeviceToken(
            memberId = memberId1,
            command = DeviceCommand.UpdateDeviceToken(deviceInfo)
        )
        
        val result2 = deviceService.updateDeviceToken(
            memberId = memberId2,
            command = DeviceCommand.UpdateDeviceToken(deviceInfo)
        )

        // then
        assertAll(
            { assertThat(deviceRepository.count()).isEqualTo(2L) }, // 2개 디바이스
            { assertThat(result1.memberId).isEqualTo(memberId1) },
            { assertThat(result2.memberId).isEqualTo(memberId2) },
            { assertThat(result1.deviceId).isEqualTo(result2.deviceId) }, // 같은 deviceId
            { assertThat(result1.deviceToken).isEqualTo(result2.deviceToken) }, // 같은 토큰
        )
    }

    @Test
    fun `다른 deviceId는 별도의 디바이스로 저장된다`() {
        // given
        val memberId = 1L
        val deviceInfo1 = createDeviceInfoFixture(deviceId = "device1")
        val deviceInfo2 = createDeviceInfoFixture(deviceId = "device2")

        // when
        val result1 = deviceService.updateDeviceToken(
            memberId = memberId,
            command = DeviceCommand.UpdateDeviceToken(deviceInfo1)
        )
        
        val result2 = deviceService.updateDeviceToken(
            memberId = memberId,
            command = DeviceCommand.UpdateDeviceToken(deviceInfo2)
        )

        // then
        assertAll(
            { assertThat(deviceRepository.count()).isEqualTo(2L) }, // 2개 디바이스
            { assertThat(result1.memberId).isEqualTo(result2.memberId) }, // 같은 memberId
            { assertThat(result1.deviceId).isEqualTo("device1") },
            { assertThat(result2.deviceId).isEqualTo("device2") },
        )
    }
}