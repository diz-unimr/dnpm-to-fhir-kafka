import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("java")
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "de.unimarburg.diz"
version = "0.0.1-SNAPSHOT" // x-release-please-version
description = "dnpm-to-fhir-kafka"

val mtbModel = "0.6.4"
val dnpmToFhir = "0.1.0-SNAPSHOT"
val hapiFhirVersion = "7.6.1"


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    maven {
        url = uri("https://git.dnpm.dev/api/packages/public-snapshots/maven")
    }
    maven {
        url = uri("https://git.dnpm.dev/api/packages/public/maven")
    }
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("dev.pcvolkmer.mv64e:mtb-model:$mtbModel")
    implementation("dev.pcvolkmer.onco:dnpm-to-fhir:$dnpmToFhir")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:$hapiFhirVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiFhirVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName.set("ghcr.io/diz-unimr/dnpm-to-fhir-kafka")

    // Binding for CA Certs
    bindings.set(
        listOf(
            "$rootDir/bindings/ca-certificates/:/platform/bindings/ca-certificates"
        )
    )

    environment.set(
        environment.get() + mapOf(
            // Enable this line to embed CA Certs into image on build time
            //"BP_EMBED_CERTS" to "true",
            "BP_OCI_SOURCE" to "https://github.com/diz-unimr/dnpm-to-fhir-kafka",
            "BP_OCI_LICENSES" to "AGPLv3",
            "BP_OCI_DESCRIPTION" to "Fetch patient records in DNPM-Datenmodell 2.1 from Kafka, map it to FHIR bundle and send it back to Kafka."
        )
    )
}
