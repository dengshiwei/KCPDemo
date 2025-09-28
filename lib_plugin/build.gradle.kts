plugins {
    id("java-library")
    id("java-gradle-plugin")
    id("kotlin-kapt")
    alias(libs.plugins.jetbrains.kotlin.jvm)
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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.0.21")
//    implementation(project(":lib_kcp"))
    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
    kapt("com.google.auto.service:auto-service:1.1.1") // 若用 KAPT
}

// ----------------- Gradle 插件发布配置 -----------------
gradlePlugin {
    plugins {
        create("debuglogPlugin") {
            id = "com.explore.plugin"
            version = "0.0.1"
            implementationClass = "com.explore.lib_plugin.DebugLogGradleSubPlugin"
        }
    }
}

// ----------------- 发布到本地 Maven -----------------
publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            from(components["java"])
//            groupId = "com.explore.plugin"
//            artifactId = "debuglog"
//            version = "0.0.1"
//        }
//    }

    repositories {
        maven {
            url = uri("../repo")
        }
    }
}
