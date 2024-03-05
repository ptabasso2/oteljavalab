plugins {
	java
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
}

group = "com.pej.otel"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.named("jar") {
	enabled = false
}

tasks {
	bootJar {
		archiveFileName.set("springtempsimu-0.0.1-SNAPSHOT.jar")
	}
}

