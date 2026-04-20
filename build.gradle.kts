import groovy.json.JsonOutput

plugins {
    java
    application
    eclipse
    `check-lib-versions`
    id("org.graalvm.buildtools.native") version "0.11.5"
}

allprojects {
    group = "org.asamk"
    version = "0.14.3-SNAPSHOT"
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

application {
    mainClass.set("org.asamk.signal.Main")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

val artifactType = Attribute.of("artifactType", String::class.java)
val minified = Attribute.of("minified", Boolean::class.javaObjectType)
dependencies {
    attributesSchema {
        attribute(minified)
    }
    artifactTypes.getByName("jar") {
        attributes.attribute(minified, false)
    }
}

configurations.runtimeClasspath.configure {
    attributes {
        attribute(minified, true)
    }
}

val excludePatterns = mapOf(
    "libsignal-client" to setOf(
        "libsignal_jni_testing_amd64.so",
        "signal_jni_testing_amd64.dll",
        "libsignal_jni_testing_amd64.dylib",
        "libsignal_jni_testing_aarch64.dylib",
    )
)

val schemaAnnotationProcessor by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    registerTransform(JarFileExcluder::class) {
        from.attribute(minified, false).attribute(artifactType, "jar")
        to.attribute(minified, true).attribute(artifactType, "jar")

        parameters {
            excludeFilesByArtifact = excludePatterns
        }
    }

    schemaAnnotationProcessor(libs.micronaut.json.schema.processor)
    schemaAnnotationProcessor(libs.micronaut.inject.java)
    implementation(libs.bouncycastle)
    implementation(libs.jackson.databind)
    implementation(libs.argparse4j)
    implementation(libs.dbusjava)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.jul)
    implementation(libs.logback)
    implementation(libs.zxing)
    implementation(libs.micronaut.json.schema.annotations)
    if (gradle.startParameter.taskNames.any { it.contains("jsonSchemas") }) {
        implementation(libs.micronaut.json.schema.generator)
    }
    implementation(project(":libsignal-cli"))

    testImplementation(libs.junit.jupiter)
    testImplementation(platform(libs.junit.jupiter.bom))
    testRuntimeOnly(libs.junit.launcher)
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

tasks.withType<Jar> {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Main-Class" to application.mainClass.get(),
            "Enable-Native-Access" to "ALL-UNNAMED",
        )
    }
}

graalvmNative {
    toolchainDetection.set(false)
    binaries {
        create("linuxAmd64") {
            buildArgs.add("--platform")
            buildArgs.add("linux/amd64")
            buildArgs.add("-Dfile.encoding=UTF-8")
        }
        create("linuxArm64") {
            buildArgs.add("--platform")
            buildArgs.add("linux/aarch64")
            buildArgs.add("-Dfile.encoding=UTF-8")
        }
        create("macosArm64") {
            buildArgs.add("--platform")
            buildArgs.add("darwin/aarch64")
            buildArgs.add("-Dfile.encoding=UTF-8")
        }
        create("macosAmd64") {
            buildArgs.add("--platform")
            buildArgs.add("darwin/amd64")
            buildArgs.add("-Dfile.encoding=UTF-8")
        }
        create("windowsAmd64") {
            buildArgs.add("--platform")
            buildArgs.add("windows/amd64")
            buildArgs.add("-Dfile.encoding=UTF-8")
        }
    }
}

}
