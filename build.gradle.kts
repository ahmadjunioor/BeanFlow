// build.gradle.kts (Project Level)
plugins {
    // Biarkan 3 baris alias ini (JANGAN DIHAPUS)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Tambahkan baris Firebase ini:
    id("com.google.gms.google-services") version "4.4.2" apply false
}