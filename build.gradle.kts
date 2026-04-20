import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask

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

// Manually register nativeCompile tasks for each binary since the GraalVM plugin's
// automatic task registration doesn't work properly with Gradle 9.4's lazy configuration
// The graalvmNative.binaries container is lazy, and the plugin's configureEach
// callbacks don't fire when binaries are created in the build script.
afterEvaluate {
    val graalExt = project.extensions.getByType(org.graalvm.buildtools.gradle.dsl.GraalVMExtension::class.java)
    for (binaryName in listOf("linuxAmd64", "linuxArm64", "macosArm64", "macosAmd64", "windowsAmd64")) {
        val binary = graalExt.binaries.getByName(binaryName)
        val taskName = if (binaryName == "main") "nativeCompile" else "nativeCompile${binaryName.replaceFirstChar { it.uppercase() }}"
        project.tasks.register(taskName, BuildNativeImageTask::class.java) { task ->
            task.group = "build"
            task.description = "Compiles a native image for the $binaryName binary"
            task.options.convention(binary)
        }
        println("Registered task: $taskName for binary: $binaryName")
    }
}
