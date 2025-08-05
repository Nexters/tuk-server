package nexters.tuk.application.purpose.dto.response

import java.io.Serializable

class PurposeResponse {
    data class Purposes(
        val whenTags: List<String>,
        val whereTags: List<String>,
        val whatTags: List<String>,
    ) : Serializable
}