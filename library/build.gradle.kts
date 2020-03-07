import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.library")
    id("maven-publish")
    id("kotlin-android")
    id("com.jfrog.bintray").version("1.8.4")
}

group = "io.maryk.rocksdb"
version = "6.8.0"

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.0")
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
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
                targets += "rocksdbjni"
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
            setPath("../rocksdb/CMakeLists.txt")
            setVersion("3.10.2")
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
            java.srcDirs("src/androidTest/java")
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
    implementation("io.maryk.lz4:lz4-android:1.9.2")
    androidTestImplementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

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
    }

    fun findProperty(s: String) = project.findProperty(s) as String?
    bintray {
        user = findProperty("bintrayUser")
        key = findProperty("bintrayApiKey")
        publish = true
        pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
            repo = "maven"
            name = "rocksdb-android"
            userOrg = "maryk"
            setLicenses("Apache-2.0")
            setPublications(*project.publishing.publications.names.toTypedArray())
            vcsUrl = "https://github.com/marykdb/rocksdb-android.git"
        })
    }

    project.publishing.publications.withType<MavenPublication>().forEach { publication ->
        publication.pom.withXml {
            asNode().apply {
                appendNode("name", project.name)
                appendNode("description", "Android RocksDB library")
                appendNode("url", "https://github.com/marykdb/rocksdb-android")
                appendNode("licenses").apply {
                    appendNode("license").apply {
                        appendNode("name", "The Apache Software License, Version 2.0")
                        appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
                        appendNode("distribution", "repo")
                    }
                }
                appendNode("developers").apply {
                    appendNode("developer").apply {
                        appendNode("id", "jurmous")
                        appendNode("name", "Jurriaan Mous")
                    }
                }
                appendNode("scm").apply {
                    appendNode("url", "https://github.com/marykdb/rocksdb-android.git")
                }
            }
        }
    }
}
