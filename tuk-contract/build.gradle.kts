import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${properties["swaggerVersion"]}")
}

tasks.withType(Jar::class) { enabled = true }
tasks.withType(BootJar::class) { enabled = false }