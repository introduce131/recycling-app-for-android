import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

val properties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

android {
    namespace = "com.loveprofessor.recyclingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.loveprofessor.recyclingapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "KAKAO_MAP_KEY", properties.getProperty("KAKAO_MAP_KEY"))      // 카카오맵 API KEY
            buildConfigField("String", "POINT_USAGE_URL", properties.getProperty("POINT_USAGE_URL"))  // 탄소중립포인트 사용처 API KEY
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        mlModelBinding = true
        buildConfig = true
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
    /* firebase auth 의존성 추가 */
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.1.0")

    /* fireStore, storage, functions 의존성 추가 */
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.0")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.0")
    implementation("com.google.firebase:firebase-functions")

    /* Glide 의존성 추가 */
    implementation("com.github.bumptech.glide:glide:4.13.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2")

    /* Custom Dialog */
    implementation("com.github.f0ris.sweetalert:library:1.5.6")

    /* MPAndroidChart */
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    /* kakao맵 */
    implementation("com.kakao.maps.open:android:2.9.5")

    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}