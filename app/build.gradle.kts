plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.bitwisearts.example"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.bitwisearts.example"
        minSdk = 29
        targetSdk = 33
        versionCode=  1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }
    project.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "11"
            languageVersion = "1.8"
        }
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val composeUiVersion = "1.3.3"
val composeVersion = "1.4.0-beta01"

dependencies {

    // Standard packages
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.material:material:1.3.1")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Jetpack Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.5.3")

    // This JSON library is very straight forward, like a string builder. No
    // guessing as to the structure of the JSON.
    implementation("org.availlang:avail-json:2.1.1")

    // camera
    implementation("androidx.camera:camera-camera2:1.3.0-alpha03")
    implementation("androidx.camera:camera-view:1.3.0-alpha03")
    implementation("androidx.camera:camera-lifecycle:1.3.0-alpha03")

    // barcode
    implementation("com.google.mlkit:barcode-scanning:17.0.3")
}