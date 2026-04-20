import groovy.json.JsonOutput
import java.io.File

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

val schemaAnnotationProcessor = configurations.creating {
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

// Get GRAALVM_HOME from environment or use a default
val graalVmHome: String = System.getenv("GRAALVM_HOME")
    ?: (System.getProperty("graalvmHome")
        ?: (System.getProperty("java.home")))

val nativeArchMap = listOf(
    "LinuxAmd64"    to listOf("--platform=linux/amd64"),
    "LinuxArm64"    to listOf("--platform=linux/aarch64"),
    "MacosArm64"    to listOf("--platform=darwin/aarch64"),
    "MacosAmd64"    to listOf("--platform=darwin/amd64"),
    "WindowsAmd64"  to listOf("--platform=windows/amd64")
)

// Register custom nativeCompile tasks that invoke native-image directly
for ((arch, platformArgs) in nativeArchMap) {
    val outputDir = "build/native/nativeCompile"
    val exeName = if (arch.startsWith("Windows")) "signal-cli.exe" else "signal-cli"

    tasks.register<Exec>("nativeCompile$arch") {
        group = "build"
        description = "Compiles a native image for $arch using GraalVM at $graalVmHome"

        doFirst {
            file(outputDir).mkdirs()
        }

        executable = "$graalVmHome/bin/native-image"

        // Get classpath from runtimeClasspath
        val runtimeCp = configurations.runtimeClasspath.get()
        val classpathFiles = runtimeCp.resolve().joinToString(File.pathSeparator) { it.absolutePath }

        args("-J--enable-native-access=ALL-UNNAMED")
        args("-J-Dfile.encoding=UTF-8")
        args("-J--add-opens=java.base/java.lang=ALL-UNNAMED")
        args("-J--add-opens=java.base/java.util=ALL-UNNAMED")
        args("-J--add-opens=java.base/java.security=ALL-UNNAMED")
        for (arg in platformArgs) {
            args(arg)
        }
        args("--initialize-at-build-time=org.slf4j")
        args("--initialize-at-build-time=org.asamk.signal")
        args("-H:+ReportExceptionStackTraces")
        args("-H:Name=$exeName")
        args("-O1")
        args("-cp", classpathFiles)
        args("org.asamk.signal.Main")

        environment("JAVA_HOME", graalVmHome)
        environment("GRAALVM_HOME", graalVmHome)

        println("nativeCompile$arch: using GRAALVM_HOME=$graalVmHome, executable=$graalVmHome/bin/native-image")
    }
}
