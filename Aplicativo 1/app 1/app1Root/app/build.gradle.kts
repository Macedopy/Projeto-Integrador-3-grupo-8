plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.geowarning"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.geowarning"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = "AIzaSyBmXjrQo7iN77n1zhR7onRbikrgiB3YQgU"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")

// Localização atual
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("com.google.maps.android:maps-compose:4.1.1") // renderização de mapa com Jetpack Compose
    implementation("com.google.accompanist:accompanist-permissions:0.34.0") // para permissões
    implementation("com.google.android.gms:play-services-location:21.0.1") // localização via GPS


    // AndroidX e Jetpack
    implementation("androidx.core:core-ktx:1.10.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

// Material Design
    implementation("com.google.android.material:material:1.11.0")

// Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

// Rede (Retrofit + Gson)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Imagens (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

// Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}