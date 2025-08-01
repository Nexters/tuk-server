dependencies {
    implementation(project(":tuk-contract"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // quartz
    implementation("org.springframework.boot:spring-boot-starter-quartz:${project.properties["quartzVersion"]}")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j:${properties["mysqlVersion"]}")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${project.properties["mockitoKotlinVersion"]}")
    testImplementation("com.ninja-squad:springmockk:${project.properties["springMockkVersion"]}")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
}
