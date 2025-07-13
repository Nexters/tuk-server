dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

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
}

tasks.withType<Test> {
    useJUnitPlatform()
}