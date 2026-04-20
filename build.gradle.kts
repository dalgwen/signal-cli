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

// Platform configurations for native image builds
val nativePlatforms = mapOf(
    "LinuxAmd64"    to Triple("linux", "amd64", "signal-cli"),
    "LinuxArm64"    to Triple("linux", "aarch64", "signal-cli"),
    "MacosArm64"    to Triple("darwin", "aarch64", "signal-cli"),
    "MacosAmd64"    to Triple("darwin", "amd64", "signal-cli"),
    "WindowsAmd64"  to Triple("windows", "amd64", "signal-cli.exe")
)

for ((taskName, config) in nativePlatforms) {
    val (os, arch, binaryName) = config
    val outputDir = "build/native/nativeCompile"

    tasks.register<Exec>("nativeCompile$taskName") {
        group = "build"
        description = "Compiles a native image for $taskName"

        doFirst {
            file(outputDir).mkdirs()
        }

        val graalVmHome = System.getenv("GRAALVM_HOME") ?: "/usr/local/graalvm"
        executable = "$graalVmHome/bin/native-image"

        // Get classpath from the runtime classpath
        val runtimeCp = configurations.runtimeClasspath.get()
        val classpathFiles = runtimeCp.resolve().joinToString(File.pathSeparator) { it.absolutePath }

        args("-cp", classpathFiles)
        args("--platform", "$os/$arch")
        args("-Dfile.encoding=UTF-8")
        args("--enable-native-access=ALL-UNNAMED")
        args("-march=compatibility")
        args("-o", file("$outputDir/$binaryName").absolutePath)
        args("org.asamk.signal.Main")

        environment("JAVA_HOME", graalVmHome)
    }
}
