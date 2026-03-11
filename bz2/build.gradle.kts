@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
}

val bz2Version = "1.0.8"
val bz2Sha = "ab5a03176ee106d3f0fa90e381da478ddae405918153cca248e682cd0c4a2269"

group = "io.maryk.bz2"
version = bz2Version

val bz2Home = projectDir.resolve("bzip2-$bz2Version")

android {
    namespace = "bz2"
    compileSdk = 36
    ndkVersion = "27.0.12077973"
    defaultConfig {
        minSdk = 21
        externalNativeBuild {
            cmake {
                targets.add("bz2")
                arguments.add("-DBZ2_PATH=${bz2Home.absolutePath}")
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
            path = File("$projectDir/CMakeLists.txt")
            version = "3.31.3"
        }
    }
}

val downloadBz2 = tasks.register<Exec>("downloadBz2") {
    workingDir = projectDir
    commandLine("./downloadBz2.sh", bz2Version, bz2Sha)
}

tasks.withType<com.android.build.gradle.tasks.ExternalNativeBuildJsonTask> {
    dependsOn(downloadBz2)
}
