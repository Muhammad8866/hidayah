import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

// `-Pfoss` (used by F-Droid) builds only the foss flavor, with no Google/Firebase plugin applied.
val fossOnly = providers.gradleProperty("foss").isPresent
if (!fossOnly) {
  apply(plugin = "com.google.gms.google-services")
  apply(plugin = "com.google.firebase.crashlytics")
}

// New UI copy is translated once at build time and bundled for offline lookup.
// The API credential is read only by Python from TRANSLATION_API_KEY.
val autoTranslationDir = layout.buildDirectory.dir("generated/autoTranslations")
val generateAutoTranslations = tasks.register<Exec>("generateAutoTranslations") {
  group = "localization"
  description = "Translate new UI phrases into every registered app language"
  workingDir(rootDir)
  commandLine(providers.gradleProperty("translationPython").getOrElse("python"),
    "tools/auto_translate.py", "--output", autoTranslationDir.get().asFile.absolutePath)
}
tasks.configureEach {
  if (name == "preBuild" || name.startsWith("ksp") || name.startsWith("compile")) {
    dependsOn(generateAutoTranslations)
  }
}

android {
  namespace = "com.example"
  sourceSets.getByName("main").kotlin.srcDir(autoTranslationDir.get().asFile)
  if (!fossOnly) assetPacks += listOf(":hindi_quran_pdf")
  sourceSets.getByName("debug").assets.srcDir("${rootDir}/hindi_quran_pdf/src/main/assets")
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.deencompanion.quranj"
    minSdk = 24
    targetSdk = 36
    versionCode = 4
    versionName = "1.3"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // play: Google Play build with Firebase, Play services and Play delivery.
  // foss: F-Droid build with only free dependencies; Firebase-backed features are hidden.
  flavorDimensions += "store"
  productFlavors {
    create("play") { dimension = "store" }
    create("foss") { dimension = "store" }
  }
  sourceSets.getByName("foss").assets.srcDir("${rootDir}/hindi_quran_pdf/src/main/assets")
  // The foss build copies the bundled Hindi PDF out of the APK through a file descriptor.
  androidResources { noCompress += "pdf" }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      val keystoreProps = Properties()
      val keystorePropsFile = file("${rootDir}/keystore.properties")
      if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { keystoreProps.load(it) }
      storePassword = System.getenv("STORE_PASSWORD") ?: keystoreProps.getProperty("storePassword")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD") ?: keystoreProps.getProperty("keyPassword")
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // F-Droid signs with its own key, so an unsigned release is built when no keystore is present.
      if (signingConfigs.getByName("release").storeFile?.exists() == true) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
    // Keep the default Android debug signing configuration. It creates and uses
    // the standard per-user debug keystore automatically when one is not present.
    debug { }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// F-Droid builds never configure the play variants, so no Google artifact is resolved.
androidComponents {
  beforeVariants { variant ->
    if (fossOnly && variant.productFlavors.any { it.second == "play" }) variant.enable = false
  }
}
// The Google plugins see every variant; the foss variants have no Firebase to report to.
tasks.configureEach {
  if (name.contains("Foss") && (name.contains("Crashlytics") || name.contains("GoogleServices"))) enabled = false
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}


// AGP creates flavor+build-type configurations lazily; App Check's debug provider needs this one now.
configurations.maybeCreate("playDebugImplementation")

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  "playImplementation"("com.google.android.libraries.places:places:5.1.1")
  implementation(platform(libs.androidx.compose.bom))
  "playImplementation"(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai) // unused: the assistant is an offline, source-constrained lookup
  // Firestore + Anonymous Auth are used only by the Community Masjid feature.
  "playImplementation"(libs.firebase.firestore)
  "playImplementation"(libs.firebase.auth)
  "playImplementation"(libs.firebase.analytics)
  // Crash reports and automatic app-start/frame traces; no network-instrumentation plugin.
  "playImplementation"(libs.firebase.crashlytics)
  "playImplementation"(libs.firebase.perf)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // (firebase-auth is enabled above for anonymous sign-in)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  // App Check: Play Integrity in release, the debug provider only in debug builds.
  "playImplementation"(libs.firebase.appcheck.playintegrity)
  "playDebugImplementation"(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  // ExoPlayer seeks CBR MP3s exactly; MediaPlayer uses the coarse Info-header table and drifts.
  implementation(libs.media3.exoplayer)
  implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.pdfbox.android)
  "playImplementation"(libs.play.services.location)
  "playImplementation"("com.google.android.play:asset-delivery:2.3.0")
  // Mandatory (immediate) in-app updates from Google Play.
  "playImplementation"("com.google.android.play:app-update:2.1.0")
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
