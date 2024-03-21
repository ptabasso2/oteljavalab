plugins {
	java
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
}

configurations.all {
	resolutionStrategy.eachDependency {
		if (requested.group == "io.opentelemetry" && requested.name !in listOf("opentelemetry-semconv","opentelemetry-api-events", "opentelemetry-extension-incubator")) {
			useVersion("1.35.0")

		}
	}
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
	implementation("io.opentelemetry:opentelemetry-api")
	implementation("io.opentelemetry:opentelemetry-sdk")
	implementation("io.opentelemetry:opentelemetry-exporter-logging")
	implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.23.1-alpha")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.35.0")
	implementation("io.opentelemetry:opentelemetry-sdk-metrics:1.35.0")
	implementation("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:2.1.0-alpha")

}


tasks.named("jar") {
	enabled = false
}


tasks {
	bootJar {
		archiveFileName.set("springotel-0.0.1-SNAPSHOT.jar")
	}
}
