package nexters.tuk.infrastructure.jpa

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringSetConverter(
    private val objectMapper: ObjectMapper
) : AttributeConverter<Set<String>, String> {

    override fun convertToDatabaseColumn(attribute: Set<String>?): String {
        return try {
            objectMapper.writeValueAsString(attribute ?: emptySet<String>())
        } catch (e: Exception) {
            throw IllegalArgumentException("Set을 JSON 문자열로 변환하는 데 실패했습니다.", e)
        }
    }

    override fun convertToEntityAttribute(dbData: String?): Set<String> {
        return try {
            if (dbData.isNullOrBlank()) emptySet()
            else objectMapper.readValue(dbData, object : TypeReference<Set<String>>() {})
        } catch (e: Exception) {
            throw IllegalArgumentException("JSON 문자열을 Set으로 역직렬화하는 데 실패했습니다.", e)
        }
    }
}