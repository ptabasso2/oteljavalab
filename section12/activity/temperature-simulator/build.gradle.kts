plugins {
	java
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
}

configurations.all {
	resolutionStrategy.eachDependency {
		if (requested.group == "io.opentelemetry" && requested.name !in listOf("opentelemetry-semconv","opentelemetry-api-events", "opentelemetry-extension-incubator")) {
			useVersion("1.36.0")
		}
	}
}

group = "io.temp.simulator"
version = "0.0.1"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("io.opentelemetry:opentelemetry-api")
	implementation("io.opentelemetry:opentelemetry-sdk")
	implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.24.0-alpha")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp")
}

tasks.named("jar") {
	enabled = false
}

tasks {
	bootJar {
		archiveFileName.set("tempsimulator-0.0.1.jar")
	}
}
