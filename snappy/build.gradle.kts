@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
}

group = "io.maryk.snappy"
version = "1.2.1"

android {
    namespace = "snappy"
    compileSdk = 36
    defaultConfig {
        minSdk = 21
        externalNativeBuild {
            cmake {
                targets.add("snappy")
                arguments.addAll(listOf(
                    "-DSNAPPY_BUILD_BENCHMARKS=OFF",
                    "-DBUILD_SHARED_LIBS=1",
                    "-DSNAPPY_HAVE_NEON=OFF",
                    "-DSNAPPY_BUILD_TESTS=OFF",
                    "-Wno-dev",
                ))
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
            path = File("CMakeLists.txt")
            version = "3.31.3"
        }
    }
}

val downloadSnappy by tasks.creating(Exec::class) {
    workingDir = projectDir
    commandLine("./downloadSnappy.sh", version)
}

tasks.withType<com.android.build.gradle.tasks.ExternalNativeBuildJsonTask> {
    dependsOn(downloadSnappy)
}
