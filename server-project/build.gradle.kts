import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm") version "latest.release"
    kotlin("kapt") version "latest.release"
    java
    idea
    `java-library`
    id("org.springframework.boot") version "latest.release"
    id("io.spring.dependency-management") version "latest.release"
}

group = "de.hype.hypenotify.server"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.projectlombok:lombok")
    testImplementation(kotlin("test"))
    //implementation("org.apache.logging.log4j:log4j-Core.INSTANCE:2.20.0")
    implementation("com.google.code.gson:gson:latest.release")
    implementation("com.mysql:mysql-connector-j")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:latest.release")
    //implementation("io.github.JDA-Fork:JDA:82d7ab90d6")
    implementation("net.dv8tion:JDA:latest.release")
    implementation("com.vdurmont:emoji-java:latest.release")
    implementation("org.commonmark:commonmark:latest.release")
    implementation("org.reflections:reflections:latest.release")
    implementation("com.github.valb3r.letsencrypt-helper:letsencrypt-helper-tomcat:0.5.0")
    // https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk16
    implementation("org.bouncycastle:bcprov-jdk18on:latest.release")
    implementation("org.bouncycastle:bcpkix-jdk18on:latest.release")
    implementation("org.slf4j:log4j-over-slf4j")
    implementation("p6spy:p6spy:latest.release")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("com.github.HacktheTime:HypixelAPI:4f36fb94e2d393121e7b0181d4e1f6b99b59394e")
    testImplementation("org.jsoup:jsoup:1.15.3")
    implementation("com.github.kwhat:jnativehook:2.2.2") // Global Screen Listener for Debug Key
    //    implementation("com.sun.mail:javax.mail")
    implementation("me.nullicorn:Nedit:latest.release")
    implementation("org.apache.commons:commons-lang3:latest.release")
    implementation("org.apache.commons:commons-text:latest.release")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("ch.qos.logback:logback-classic")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.jsoup:jsoup:latest.release")
    implementation(kotlin("stdlib"))
    implementation("com.google.firebase:firebase-messaging:latest.release")
    implementation("com.google.firebase:firebase-admin:latest.release")
    implementation("mysql:mysql-connector-java:latest.release")

    implementation("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
    annotationProcessor("com.google.auto.service:auto-service:1.0-rc7")

    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.madgag:animated-gif-lib:latest.release")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.named<BootJar>("bootJar") {
    mainClass.set("de.hype.hypenotify.server.Main")
    archiveFileName.set("hypenotifyonlineboot.jar")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-AprojectDir=${project.projectDir}")
}