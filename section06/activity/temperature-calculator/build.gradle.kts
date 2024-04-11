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
	implementation("io.opentelemetry:opentelemetry-api")
	implementation("io.opentelemetry:opentelemetry-sdk")
	implementation("io.opentelemetry:opentelemetry-exporter-logging")
	implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.23.1-alpha")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.35.0")
}

dependencyManagement {
	imports {
		mavenBom("io.opentelemetry:opentelemetry-bom:1.35.0")
	}
}

tasks.named("jar") {
	enabled = false
}

tasks {
	bootJar {
		archiveFileName.set("springtempcalc-0.0.1-SNAPSHOT.jar")
	}
}
