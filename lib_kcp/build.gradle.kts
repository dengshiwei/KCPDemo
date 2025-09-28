plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("kotlin-kapt")
    `maven-publish`
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.24")
    // Gradle Kotlin 插件 API
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")

    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
    kapt("com.google.auto.service:auto-service:1.1.1") // 若用 KAPT
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.explore.debuglog.kcp"
            artifactId = "plugin-kcp"
            version = "0.0.1"
        }
    }

    repositories {
        maven {
            url = uri("../repo")
        }
    }
}
