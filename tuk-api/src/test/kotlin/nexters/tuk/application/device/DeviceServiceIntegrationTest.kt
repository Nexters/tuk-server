package nexters.tuk.application.device

import nexters.tuk.application.device.dto.request.DeviceCommand
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

        deviceService.updateDeviceToken(
            memberId = memberId,
            command = DeviceCommand.UpdateDeviceToken(deviceInfo)
        )

        // when
        val actual = deviceService.updateDeviceToken(
            memberId = memberId,
            command = DeviceCommand.UpdateDeviceToken(updateDeviceInfo)
        )

        // then
        assertAll(
            { assertThat(actual.deviceToken).isEqualTo(updateDeviceInfo.deviceToken) },
            { assertThat(actual.memberId).isEqualTo(memberId) },
        )
    }
}