package nexters.tuk.infrastructure.jpa

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter(
    private val objectMapper: ObjectMapper
) : AttributeConverter<List<String>, String> {

    override fun convertToDatabaseColumn(attribute: List<String>?): String {
        return try {
            objectMapper.writeValueAsString(attribute ?: emptyList<String>())
        } catch (e: Exception) {
            throw IllegalArgumentException("리스트를 JSON 문자열로 변환하는 데 실패했습니다.", e)
        }
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        return try {
            if (dbData.isNullOrBlank()) emptyList()
            else objectMapper.readValue(dbData, object : TypeReference<List<String>>() {})
        } catch (e: Exception) {
            throw IllegalArgumentException("JSON 문자열을 리스트로 역직렬화하는 데 실패했습니다.", e)
        }
    }
}