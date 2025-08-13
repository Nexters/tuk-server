package nexters.tuk.config

import com.slack.api.Slack
import com.slack.api.methods.MethodsClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SlackConfig(
    @Value("\${slack.tokens.error-alert}")
    private val errorAlertSlackToken: String
) {
    @Bean
    fun errorAlertSlackClient(): MethodsClient {
        return Slack.getInstance().methods(errorAlertSlackToken)
    }
}