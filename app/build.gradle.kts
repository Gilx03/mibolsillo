import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // Necesario para que Room genere sus DAOs
}

/** Lee keystore.properties si existe (null si no). */
val propiedadesFirma: Properties? = rootProject.file("keystore.properties")
    .takeIf { it.exists() }
    ?.let { archivo -> Properties().apply { archivo.inputStream().use { load(it) } } }

android {
    namespace = "com.example.mibolsillo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.mibolsillo"
        minSdk = 26 // Android 8.0+: permite iconos adaptativos y java.time sin desugaring
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Firma del APK de release. Los datos de la llave se leen de "keystore.properties"
    // (en la raíz del proyecto, NO se sube a Git). Ver README > "Generar el APK".
    signingConfigs {
        create("release") {
            if (propiedadesFirma != null) {
                storeFile = rootProject.file(propiedadesFirma.getProperty("storeFile"))
                storePassword = propiedadesFirma.getProperty("storePassword")
                keyAlias = propiedadesFirma.getProperty("keyAlias")
                keyPassword = propiedadesFirma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Si existe la llave, la versión debug (botón Run de Android Studio) se firma con la
            // MISMA llave que el APK de release. Así Android permite instalar una encima de la otra
            // sin desinstalar la app y sin perder la base de datos del dispositivo.
            if (propiedadesFirma != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        release {
            // R8: reduce y optimiza el código (APK más pequeño y rápido)
            optimization {
                enable = true
            }
            // Si un compañero no tiene la llave, el APK se firma con la llave de depuración
            // para que igual se pueda instalar y probar.
            signingConfig = if (propiedadesFirma != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Room (base de datos local)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Navegación y preferencias
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
