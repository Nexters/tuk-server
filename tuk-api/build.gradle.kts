dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// mysql
	implementation("mysql:mysql-connector-java:${properties["mysqlVersion"]}")

	// swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${properties["swaggerVersion"]}")
}

tasks.withType<Test> {
	useJUnitPlatform()
}