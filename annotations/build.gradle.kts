plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // This module only needs the Kotlin standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}
