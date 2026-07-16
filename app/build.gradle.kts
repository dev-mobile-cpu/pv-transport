plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.pv.transport"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.pv.transport"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "1.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ✅ Release Signing
    signingConfigs {

        create("release") {
            storeFile = file("release-key.jks")
            storePassword = "pvtransport123"
            keyAlias = "key0"
            keyPassword = "pvtransport123"
            isV1SigningEnabled = true
            isV2SigningEnabled = true
        }
    }

    buildTypes {

        // ✅ Debug Build
        debug {
            //applicationIdSuffix = ".debug"
            //versionNameSuffix = "-debug"
            isDebuggable = true

        }

        release {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //isDebuggable = true
            manifestPlaceholders["android:testOnly"] = "false"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true  // ✅ enables BuildConfig.DEBUG
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // ── Core ─────────────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ── Compose ───────────────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.exifinterface)   // ✅ stable version
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.compose.material3:material3:1.3.2")

    // ── Navigation ────────────────────────────────────────────────────────────
    implementation("androidx.navigation:navigation-compose:2.7.7") // ✅ stable version



    // ── Hilt ─────────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.androidx.hilt.compiler)

    // ── Retrofit + OkHttp ────────────────────────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation(libs.okhttp.logging.interceptor)

    // ── RxJava2 ───────────────────────────────────────────────────────────────
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // ── Chucker ───────────────────────────────────────────────────────────────
    debugImplementation(libs.library)
    releaseImplementation(libs.library.no.op)

    // ── Image Loading ─────────────────────────────────────────────────────────
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ── QR Code ───────────────────────────────────────────────────────────────
    implementation("com.google.zxing:core:3.5.3")
    // ── Splash Screen ─────────────────────────────────────────────────────────
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ── Testing ───────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    implementation("io.nerdythings:okhttp-profiler:1.1.1")

    //room
    implementation ("androidx.room:room-runtime:2.8.4")
    implementation ("androidx.room:room-ktx:2.8.4")
    kapt ("androidx.room:room-compiler:2.8.4")

    // WorkManager dependency
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    implementation("io.socket:socket.io-client:2.1.0")


}