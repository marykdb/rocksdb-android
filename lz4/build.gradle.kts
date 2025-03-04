@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
}

val lz4Version = "1.10.0"

group = "io.maryk.lz4"
version = lz4Version

val lz4Home = projectDir.resolve("lz4/lz4-$lz4Version")

android {
    namespace = "lz4"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
        externalNativeBuild {
            cmake {
                targets.add("liblz4")
                arguments.add("-DLZ4_PATH=${lz4Home.absolutePath}/lib/")
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
            path = File("$projectDir/CMakeLists.txt")
            version = "3.31.3"
        }
    }
}

val downloadLz4 by tasks.creating(Exec::class) {
    workingDir = projectDir
    commandLine("./downloadLz4.sh", lz4Version)
}

tasks.withType<com.android.build.gradle.tasks.ExternalNativeBuildJsonTask> {
    dependsOn(downloadLz4)
}
