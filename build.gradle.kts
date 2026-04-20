import groovy.json.JsonOutput
import java.io.File

plugins {
    java
    application
    eclipse
    `check-lib-versions`
}

allprojects {
    group = "org.asamk"
    version = "0.14.3-SNAPSHOT"
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25

    if (!JavaVersion.current().isCompatibleWith(targetCompatibility)) {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(targetCompatibility.majorVersion))
        }
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

tasks.register("fatJar", type = Jar::class) {
    archiveBaseName.set("${project.name}-fat")
    exclude(
        "META-INF/*.SF",
        "META-INF/**/*.MF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/NOTICE*",
        "META-INF/LICENSE*",
        "META-INF/INDEX.LIST",
        "**/module-info.class",
    )
    duplicatesStrategy = DuplicatesStrategy.WARN
    doFirst {
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }
    with(tasks.jar.get())
}

tasks.register("writeLibsignalVersion") {
    doLast {
        val resolutionResult = configurations.runtimeClasspath.get().incoming.resolutionResult
        val libsignalDep =
            resolutionResult.allDependencies.find { dep -> dep.requested is ModuleComponentSelector && (dep.requested as ModuleComponentSelector).group == "org.signal" && (dep.requested as ModuleComponentSelector).moduleIdentifier.name == "libsignal-client" }
        if (libsignalDep != null) {
            val version = (libsignalDep.requested as ModuleComponentSelector).version
            file("libsignal-version").writeText(version + "\n")
        } else {
            throw GradleException("Could not find libsignal-client dependency")
        }
    }
}

tasks.register<JavaCompile>("jsonSchemas") {
    dependsOn(tasks.compileJava)
    val schemaBaseUri = "http://localhost:8080/schemas/"
    source = sourceSets.main.get().java
    include("org/asamk/signal/json/**/*.java")
    classpath = sourceSets.main.get().compileClasspath + files(sourceSets.main.get().java.destinationDirectory)
    destinationDirectory.set(layout.buildDirectory.dir("generated"))
    options.annotationProcessorPath = schemaAnnotationProcessor
    options.compilerArgs.addAll(
        listOf(
            "-Amicronaut.processing.group=org.asamk",
            "-Amicronaut.processing.module=signal-cli",
            "-Amicronaut.processing.annotations=org.asamk.signal.json.*",
            "-Amicronaut.jsonschema.baseUri=$schemaBaseUri",
        )
    )
    doLast {
        fileTree(destinationDirectory.get().dir("META-INF/schemas").asFile) {
            include("*.schema.json")
        }.forEach { schemaFile ->
            val normalized = schemaFile.readText().replace("\"$schemaBaseUri/", "\"")
            val prettyJson = JsonOutput.prettyPrint(normalized)
            schemaFile.writeText("$prettyJson\n")
        }
    }
}

// Register native image compilation tasks for each target platform
listOf(
    "linuxAmd64"    to Triple("linux", "amd64", "signal-cli"),
    "linuxArm64"    to Triple("linux", "aarch64", "signal-cli"),
    "macosArm64"    to Triple("darwin", "aarch64", "signal-cli"),
    "macosAmd64"    to Triple("darwin", "amd64", "signal-cli"),
    "windowsAmd64"  to Triple("windows", "amd64", "signal-cli.exe")
).forEach { (name, config) ->
    val (os, arch, binaryName) = config
    val outputDir = "build/native/nativeCompile"
    
    tasks.register<Exec>("nativeCompile${name.replaceFirstChar { it.uppercase() }}") {
        group = "build"
        description = "Build native image for ${name}"
        
        // Get GRAALVM_HOME from environment
        val graalVmHome = System.getenv("GRAALVM_HOME")
            ?: throw GradleException("GRAALVM_HOME environment variable not set")
        
        doFirst {
            file(outputDir).mkdirs()
        }
        
        executable = "$graalVmHome/bin/native-image"
        
        // Build classpath from runtimeClasspath
        val classpath = configurations.runtimeClasspath.get().files.joinToString(File.pathSeparator) {
            it.absolutePath
        }
        
        args("-classpath", classpath)
        args("--platform", "$os/$arch")
        args("-Dfile.encoding=UTF-8")
        args("--enable-native-access=ALL-UNNAMED")
        args("-march=compatibility")
        args("--initialize-at-build-time=org.slf4j")
        args("--initialize-at-build-time=org.asamk.signal")
        args("-H:+ReportExceptionStackTraces")
        args("-H:Name=$binaryName")
        args("-H:Path=$outputDir")
        args("-O1")
        args("org.asamk.signal.Main")
        
        environment("JAVA_HOME", graalVmHome)
        environment("GRAALVM_HOME", graalVmHome)
        
        println("Building native image for $name using GRAALVM_HOME=$graalVmHome")
    }
}
