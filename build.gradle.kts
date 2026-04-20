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

// Force the binaries to be realized by accessing each one by name
afterEvaluate {
    val graalExt = project.extensions.getByType(org.graalvm.buildtools.gradle.dsl.GraalVMExtension::class.java)
    val binaryNames = graalExt.binaries.names
    println("Registered native binaries: $binaryNames")
    for (name in binaryNames) {
        val binary = graalExt.binaries.getByName(name)
        println("Forcing binary realization: ${binary.name}")
    }
}
