package nexters.tuk.application.push

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.FileInputStream
import java.io.InputStream
import javax.annotation.PostConstruct

@Configuration
class FcmConfig(
    @Value("\${spring.app.firebase-config-file}")
    private val firebaseConfigFile: String,
) {
    @PostConstruct
    fun init() {
        val resource = ClassPathResource(firebaseConfigFile)
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(resource.inputStream))
            .build()
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }
    }
}