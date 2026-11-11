import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")

// New UI copy is translated once at build time and bundled for offline lookup.
// The API credential is read only by Python from TRANSLATION_API_KEY.
val autoTranslationDir = layout.buildDirectory.dir("generated/autoTranslations")
val generateAutoTranslations = tasks.register<Exec>("generateAutoTranslations") {
  group = "localization"
  description = "Translate new UI phrases into every registered app language"
  workingDir(rootDir)
  commandLine(providers.gradleProperty("translationPython").getOrElse("python"),
    "tools/auto_translate.py", "--output", autoTranslationDir.get().asFile.absolutePath)
  // Declared inputs/outputs let Gradle skip this task when nothing it reads has changed.
  inputs.file(rootProject.file("tools/auto_translate.py"))
  inputs.dir(rootProject.file("localization"))
  inputs.files(fileTree("src/main/java") { include("**/*.kt") }, fileTree("src/play/java") { include("**/*.kt") })
  // Only whether a key exists is recorded, never the key itself.
  inputs.property("hasTranslationKey",
    providers.environmentVariable("TRANSLATION_API_KEY").map { it.isNotBlank() }.orElse(false))
  inputs.property("translationModel", providers.environmentVariable("TRANSLATION_MODEL").orElse(""))
  outputs.dir(autoTranslationDir)
}
tasks.configureEach {
  if (name == "preBuild" || name.startsWith("ksp") || name.startsWith("compile") ||
    (name.startsWith("process") && name.endsWith("JavaRes"))) {
    dependsOn(generateAutoTranslations)
  }
}

android {
  namespace = "com.example"
  // Phrase packs ship as Java resources (auto_translations/*.tsv), not compiled Kotlin.
  sourceSets.getByName("main").resources.srcDir(autoTranslationDir.get().asFile)
  assetPacks += listOf(":hindi_quran_pdf")
  // The 43MB Hindi Quran PDF goes into debug APKs only with -PincludeHindiQuranPdf=true:
  // bundling it on every debug build slowed packaging and install.
  if (providers.gradleProperty("includeHindiQuranPdf").map(String::toBoolean).getOrElse(false)) {
    sourceSets.getByName("debug").assets.srcDir("${rootDir}/hindi_quran_pdf/src/main/assets")
  }
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.deencompanion.quranj"
    minSdk = 24
    targetSdk = 36
    versionCode = 9
    versionName = "1.4.4"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // play: Google Play build with Firebase, Play services and Play delivery.
  flavorDimensions += "store"
  productFlavors {
    create("play") { dimension = "store"; isDefault = true }
  }

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
      // An unsigned release is built when no keystore is present.
      if (signingConfigs.getByName("release").storeFile?.exists() == true) {
        signingConfig = signingConfigs.getByName("release")
      } else {
        logger.warn("Release keystore not found; building an unsigned release APK.")
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

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}


// AGP creates flavor+build-type configurations lazily; App Check's debug provider needs this one now.
configurations.maybeCreate("playDebugImplementation")

dependencies {
  "playImplementation"("com.google.android.libraries.places:places:5.1.1")
  implementation(platform(libs.androidx.compose.bom))
  "playImplementation"(platform(libs.firebase.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.converter.moshi)
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

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}
