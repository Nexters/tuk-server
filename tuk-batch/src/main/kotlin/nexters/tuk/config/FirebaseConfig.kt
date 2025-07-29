package nexters.tuk.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.*

@Configuration
class FirebaseConfig(
    @Value("\${firebase.admin-sdk}")
    private val encodedFirebaseAdminSdk: String,
) {
    @PostConstruct
    @Throws(IOException::class)
    fun firebaseApp(): FirebaseApp {
        val decodedBytes = Base64.getDecoder().decode(encodedFirebaseAdminSdk)
        val adminSdk = ByteArrayInputStream(decodedBytes)

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(adminSdk))
            .build()
        return FirebaseApp.initializeApp(options)
    }
}