plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.example.kai_content"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kai_content"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp Logging Interceptor (optional for debugging)
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)

    implementation ("com.google.android.material:material:1.8.0")

    // Glide image
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.ui.text.android)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    // ExoPlayer dependency
    implementation ("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-hls:2.19.1")

    // Coil for image loading
    implementation("io.coil-kt:coil:2.4.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}
