// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

tasks.register("lintAll") {
    group = "verification"
    description = "Runs Kotlin style, static analysis, and Android lint"
    dependsOn(
        ":app:ktlintCheck",
        ":shared:ktlintCheck",
        ":app:detekt",
        ":shared:detekt",
        ":app:lintDebug"
    )
}
