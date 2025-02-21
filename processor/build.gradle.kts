plugins {
    // Use the Kotlin JVM plugin – this module is pure JVM, not Android!
    kotlin("jvm")
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

dependencies {
    implementation(project(":annotations"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // Include AutoService to register your processor
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    //noinspection AnnotationProcessorOnCompilePath
    implementation("com.google.auto.service:auto-service:1.1.1")
    // Include the javax.annotation API for processor use
    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

// Ensure the Kotlin compile target is set correctly
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
