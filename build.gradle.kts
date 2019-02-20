import org.gradle.internal.os.OperatingSystem

plugins {
    java
    kotlin("jvm") version "1.3.20"
}

group = "numeriko"
version = "0.1"

repositories {
    mavenCentral()
    maven { setUrl("http://dl.bintray.com/tomasvolker/maven") }
    maven { url = uri("https://dl.bintray.com/openrndr/openrndr/") }
    maven { url = uri("https://jitpack.io") }
}

val openrndrVersion = "0.3.30"

val openrndrOS = when (OperatingSystem.current()) {
    OperatingSystem.WINDOWS -> "windows"
    OperatingSystem.LINUX -> "linux-x64"
    OperatingSystem.MAC_OS -> "macos"
    else -> error("unsupported OS")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testCompile("junit", "junit", "4.12")

    implementation(group = "tomasvolker", name = "numeriko-core", version = "0.0.3")
    implementation(group = "tomasvolker", name = "kyplot", version = "0.0.1")

    implementation("org.openrndr:openrndr-core:$openrndrVersion")
    implementation("org.openrndr:openrndr-extensions:$openrndrVersion")
    implementation("org.openrndr:openrndr-ffmpeg:$openrndrVersion")

    runtime("org.openrndr:openrndr-gl3:$openrndrVersion")
    runtime("org.openrndr:openrndr-gl3-natives-$openrndrOS:$openrndrVersion")

    implementation(group = "com.github.tomasvolker", name = "parallel-utils", version = "v1.0")
    
}
