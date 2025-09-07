@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
}

val zstdVersion = "1.5.7"
val zstdSha = "eb33e51f49a15e023950cd7825ca74a4a2b43db8354825ac24fc1b7ee09e6fa3"

group = "io.maryk.zstd"
version = zstdVersion

val zstdHome = projectDir.resolve("zstd-$zstdVersion")

android {
    namespace = "zstd"
    compileSdk = 36
    defaultConfig {
        minSdk = 21
        externalNativeBuild {
            cmake {
                targets.add("libzstd_shared")
                arguments.add("-DZSTD_BUILD_SHARED=ON")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    externalNativeBuild {
        cmake {
            path = File("$projectDir/zstd-${zstdVersion}/build/cmake/CMakeLists.txt")
            version = "3.31.3"
        }
    }
}

val downloadZstd by tasks.creating(Exec::class) {
    workingDir = projectDir
    commandLine("./downloadZstd.sh", zstdVersion, zstdSha)
}

tasks.withType<com.android.build.gradle.tasks.ExternalNativeBuildJsonTask> {
    dependsOn(downloadZstd)
}
