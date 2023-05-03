plugins {
    `java-library`
    `check-lib-versions`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.github.turasa", "signal-service-java", "2.15.3_unofficial_69")
    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.14.2")
    implementation("com.google.protobuf", "protobuf-javalite", "3.22.2")
    implementation("org.bouncycastle", "bcprov-jdk15on", "1.70")
    implementation("org.slf4j", "slf4j-api", "2.0.6")
    implementation("org.xerial", "sqlite-jdbc", "3.41.2.1")
    implementation("com.zaxxer", "HikariCP", "5.0.1")

    testImplementation("org.junit.jupiter", "junit-jupiter", "5.9.2")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.asamk.signal.manager")
    }
}
