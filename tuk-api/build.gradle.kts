plugins {
    id("com.google.cloud.tools.jib")
}

jib {
    from {
        image = "eclipse-temurin:21-jre"
    }
    to {
        image = System.getProperty("jib.to.image") ?: System.getenv("JIB_TO_IMAGE") ?: "tuk-api"
        tags = setOf(
            System.getProperty("jib.to.tags") ?: System.getenv("JIB_TO_TAGS") ?: "latest"
        )
        auth {
            username = System.getProperty("jib.to.auth.username") ?: System.getenv("JIB_TO_AUTH_USERNAME")
            password = System.getProperty("jib.to.auth.password") ?: System.getenv("JIB_TO_AUTH_PASSWORD")
        }
    }
    container {
        ports = listOf("8080")
        mainClass = "nexters.tuk.TukApplicationKt"
        jvmFlags = listOf(
            "-XX:InitialRAMPercentage=25.0",
            "-XX:MinRAMPercentage=25.0",
            "-XX:MaxRAMPercentage=50.0",
            "-XX:+UseG1GC",
            "-Dspring.profiles.active=prod"
        )
    }
}

dependencies {
    implementation(project(":tuk-contract"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j:${properties["mysqlVersion"]}")

    // swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${properties["swaggerVersion"]}")

    // google oauth
    implementation("com.google.api-client:google-api-client:${properties["googleApiClientVersion"]}")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:${properties["jjwtVersion"]}")
    implementation("io.jsonwebtoken:jjwt-impl:${properties["jjwtVersion"]}")
    implementation("io.jsonwebtoken:jjwt-jackson:${properties["jjwtVersion"]}")

    implementation("com.nimbusds:nimbus-jose-jwt:${properties["nimbusJwtVersion"]}")

    // firebase
    implementation("com.google.firebase:firebase-admin:${properties["firebaseVersion"]}")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${project.properties["mockitoKotlinVersion"]}")
    testImplementation("com.ninja-squad:springmockk:${project.properties["springMockkVersion"]}")

    // testcontainers
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("com.redis:testcontainers-redis")
    testImplementation("org.testcontainers:mysql")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
}

tasks.withType<Jar> {
    archiveBaseName.set("tuk-api")
}