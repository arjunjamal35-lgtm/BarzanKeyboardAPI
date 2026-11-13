plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 namespace = "com.barzan.keyboard"; compileSdk = 35
 defaultConfig { applicationId = "com.barzan.keyboard"; minSdk = 26; targetSdk = 35; versionCode = 5; versionName = "2.3" } }
