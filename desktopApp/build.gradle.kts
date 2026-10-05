import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

// 开发时 gradlew run 用窗口模式，打包后的正式程序默认全屏 kiosk（见 main.kt）。
tasks.withType<JavaExec>().matching { it.name == "run" }.configureEach {
    systemProperty("healthcabin.windowed", "true")
}

compose.desktop {
    application {
        mainClass = "com.lkhealth.healthcabinui.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.lkhealth.healthcabinui"
            packageVersion = "1.0.0"
        }
    }
}