plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
id("com.google.gms.google-services")

}


android {
    namespace = "com.example.manajemenbarangapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.manajemenbarangapp"
        minSdk = 25
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation ("com.google.firebase:firebase-bom:32.3.1")
    implementation ("com.google.firebase:firebase-auth-ktx:20.0.2")
    implementation ("com.google.firebase:firebase-firestore-ktx:25.1.1")
    implementation ("com.google.firebase:firebase-storage-ktx:21.0.1")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.airbnb.android:lottie:5.2.0")
    implementation(libs.firebase.database.ktx)

    kapt ("com.github.bumptech.glide:compiler:4.12.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}