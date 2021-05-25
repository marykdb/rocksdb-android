import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import java.util.*

plugins {
    id("com.android.library")
    id("maven-publish")
    id("signing")
    id("kotlin-android")
}

group = "io.maryk.rocksdb"
version = "6.11.4"

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 1
        versionName = version as String
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                arguments.addAll(
                    arrayOf(
                        "-DWITH_GFLAGS=NO",
                        "-DCMAKE_POSITION_INDEPENDENT_CODE=ON",
                        "-DWITH_TESTS=OFF",
                        "-DANDROID_STL=c++_shared",
                        "-DPORTABLE=ON",
                        "-DWITH_TESTS=OFF",
                        "-DWITH_TOOLS=OFF",
                        "-DWITH_JNI=ON",
                        "-Wno-error"
                    )
                )
                targets.add("rocksdbjni")
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
            path = File("$projectDir/../rocksdb/CMakeLists.txt")
            version = "3.18.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets {
        val main by getting {
            java.srcDirs("../rocksdb/java/src/main/java")
        }
        val androidTest by getting {
            java.srcDirs("src/androidTest/kotlin")
        }
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

val javadoc by tasks.creating(Javadoc::class) {
    source(android.sourceSets["main"].java.srcDirs)
    classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    from(javadoc.destinationDir)
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}

dependencies {
    implementation("io.maryk.lz4:lz4-android:1.9.3")
    androidTestImplementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

fun getExtraString(name: String) = ext[name]?.toString()

afterEvaluate {
    val publishTasks = mutableListOf<Jar>()

    android.libraryVariants.all { variant ->
        val name = variant.buildType.name
        if (name != com.android.builder.core.BuilderConstants.DEBUG) {
            val task = project.tasks.create<Jar>("jar${name.capitalize()}") {
                dependsOn(variant.javaCompileProvider)
                dependsOn(variant.externalNativeBuildProviders)
                from(variant.javaCompileProvider.get().destinationDir)
                from("${buildDir.absolutePath}/intermediates/library_and_local_jars_jni/$name") {
                    include("**/*.so")
                    into("lib")
                }
            }
            publishTasks.add(task)
            artifacts.add("archives", task)
        }
        true
    }

    publishing {
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = getExtraString("ossrhUsername")
                    password = getExtraString("ossrhPassword")
                }
            }
        }

        publications {
            register<MavenPublication>("RocksDB-Android").configure {
                artifact(sourcesJar)
                artifact(javadocJar)
                publishTasks.forEach(::artifact)
                groupId = project.group as String
                artifactId = "rocksdb-android"
                version = project.version as String

                //The publication doesn't know about our dependencies, so we have to manually add them to the pom
                pom.withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")

                    //Iterate over the compile dependencies (we don't want the test ones), adding a <dependency> node for each
                    configurations.implementation.get().allDependencies.forEach {
                        dependenciesNode.appendNode ("dependency").apply {
                            appendNode("groupId", it.group)
                            appendNode("artifactId", it.name)
                            appendNode("version", it.version)
                        }
                    }
                }
            }
        }

        publications.withType<MavenPublication> {
            pom {
                name.set(project.name)
                description.set("Android RocksDB library")
                url.set("https://github.com/marykdb/rocksdb-android")

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("jurmous")
                        name.set("Jurriaan Mous")
                    }
                }
                scm {
                    url.set("https://github.com/marykdb/rocksdb-android.git")
                }
            }
        }
    }

    signing {
        sign(publishing.publications)
    }
}
