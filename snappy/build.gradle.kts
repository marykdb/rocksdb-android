@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
}

val snappyVersion = "1.2.2"
val snappySha = "90f74bc1fbf78a6c56b3c4a082a05103b3a56bb17bca1a27e052ea11723292dc"

group = "io.maryk.snappy"
version = snappyVersion

android {
    namespace = "snappy"
    compileSdk = 36
    ndkVersion = "29.0.14206865"
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
                    "-DCMAKE_WARN_DEPRECATED=FALSE",
                    "-Wno-dev",
                ))
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    externalNativeBuild {
        cmake {
            path = File("CMakeLists.txt")
            version = "3.31.3"
        }
    }
}

val downloadSnappy = tasks.register<Exec>("downloadSnappy") {
    workingDir = projectDir
    commandLine("./downloadSnappy.sh", snappyVersion, snappySha)
}

tasks.withType<com.android.build.gradle.tasks.ExternalNativeBuildJsonTask> {
    dependsOn(downloadSnappy)
}
