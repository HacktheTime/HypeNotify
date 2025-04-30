plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Add the Kotlin annotation processor plugin
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "de.hype.hypenotify"
    compileSdk = 35


    defaultConfig {
        applicationId = "de.hype.hypenotify"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Moved multiDexEnabled out of nested defaultConfig block
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_18.toString()
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
    implementation("com.google.firebase:firebase-messaging:24.1.1")
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.google.code.gson:gson:latest.release")
    implementation("org.reflections:reflections:latest.release")
    // Instead, add a dependency on your annotation processor:
    kapt(project(":processor"))
    // If you moved your annotation definitions into a separate module, include it:
    implementation(project(":annotations"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}