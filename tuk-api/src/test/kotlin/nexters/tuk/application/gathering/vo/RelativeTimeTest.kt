package nexters.tuk.application.gathering.vo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RelativeTimeTest {

    @Test
    fun `오늘을 표현할 수 있다`() {
        val relativeTime = RelativeTime.fromDays(0)
        
        assertThat(relativeTime.value).isEqualTo("오늘")
    }

    @Test
    fun `일 단위로 표현할 수 있다`() {
        val oneDay = RelativeTime.fromDays(1)
        val sixDays = RelativeTime.fromDays(6)
        
        assertThat(oneDay.value).isEqualTo("1일 전")
        assertThat(sixDays.value).isEqualTo("6일 전")
    }

    @Test
    fun `주 단위로 표현할 수 있다`() {
        val oneWeek = RelativeTime.fromDays(7)
        val twoWeeks = RelativeTime.fromDays(14)
        val threeWeeks = RelativeTime.fromDays(21)
        val fourWeeks = RelativeTime.fromDays(28)
        
        assertThat(oneWeek.value).isEqualTo("1주 전")
        assertThat(twoWeeks.value).isEqualTo("2주 전")
        assertThat(threeWeeks.value).isEqualTo("3주 전")
        assertThat(fourWeeks.value).isEqualTo("4주 전")
    }

    @Test
    fun `개월 단위로 표현할 수 있다`() {
        val oneMonth = RelativeTime.fromDays(30)
        val twoMonths = RelativeTime.fromDays(60)
        val sixMonths = RelativeTime.fromDays(180)
        val elevenMonths = RelativeTime.fromDays(330)
        
        assertThat(oneMonth.value).isEqualTo("1개월 전")
        assertThat(twoMonths.value).isEqualTo("2개월 전")
        assertThat(sixMonths.value).isEqualTo("6개월 전")
        assertThat(elevenMonths.value).isEqualTo("11개월 전")
    }

    @Test
    fun `년 단위로 표현할 수 있다`() {
        val oneYear = RelativeTime.fromDays(365)
        val twoYears = RelativeTime.fromDays(730)
        val fiveYears = RelativeTime.fromDays(1825)
        
        assertThat(oneYear.value).isEqualTo("1년 전")
        assertThat(twoYears.value).isEqualTo("2년 전")
        assertThat(fiveYears.value).isEqualTo("5년 전")
    }

    @Test
    fun `JSON 직렬화 시 value 값이 직접 사용된다`() {
        val relativeTime = RelativeTime.fromDays(0)
        val objectMapper = jacksonObjectMapper()
        
        val json = objectMapper.writeValueAsString(relativeTime)
        
        assertThat(json).isEqualTo("\"오늘\"")
    }

    @Test
    fun `JSON 역직렬화가 가능하다`() {
        val objectMapper = jacksonObjectMapper()
        val json = "\"3일 전\""
        
        val relativeTime = objectMapper.readValue(json, RelativeTime::class.java)
        
        assertThat(relativeTime.value).isEqualTo("3일 전")
    }

    @Test
    fun `경계값 테스트 - 6일과 7일`() {
        val sixDays = RelativeTime.fromDays(6)
        val sevenDays = RelativeTime.fromDays(7)
        
        assertThat(sixDays.value).isEqualTo("6일 전")
        assertThat(sevenDays.value).isEqualTo("1주 전")
    }

    @Test
    fun `경계값 테스트 - 29일과 30일`() {
        val twentyNineDays = RelativeTime.fromDays(29)
        val thirtyDays = RelativeTime.fromDays(30)
        
        assertThat(twentyNineDays.value).isEqualTo("4주 전")
        assertThat(thirtyDays.value).isEqualTo("1개월 전")
    }

    @Test
    fun `경계값 테스트 - 364일과 365일`() {
        val threeSixtyFourDays = RelativeTime.fromDays(364)
        val threeSixtyFiveDays = RelativeTime.fromDays(365)
        
        assertThat(threeSixtyFourDays.value).isEqualTo("12개월 전")
        assertThat(threeSixtyFiveDays.value).isEqualTo("1년 전")
    }
}