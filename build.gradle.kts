plugins {
    java
    application
}

group = "com.cubiclauncher"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    flatDir {
        dirs("libs")
    }
}

val fxVersion = "21.0.4"

val os = org.gradle.internal.os.OperatingSystem.current()
val platform = when {
    os.isWindows -> "win"
    os.isLinux -> "linux"
    os.isMacOsX -> "mac"
    else -> throw GradleException("Unsupported platform for JavaFX")
}

dependencies {
    // JavaFX
    implementation("org.openjfx:javafx-base:$fxVersion:$platform")
    implementation("org.openjfx:javafx-controls:$fxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$fxVersion:$platform")
    implementation("org.openjfx:javafx-fxml:$fxVersion:$platform")

    // Aplicación
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.cubiclauncher:claunch:1.0.0")
    implementation("net.java.dev.jna:jna:5.18.1")
    implementation("net.java.dev.jna:jna-platform:5.18.1")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.21")
    implementation("org.slf4j:slf4j-api:2.0.17")

    // Testing - JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1") // Esta es la que faltaba!

    // Testing - Mockito
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
}

application {
    mainClass.set("com.cubiclauncher.launcher.Launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
    }
}

tasks.named<JavaExec>("run") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })

    jvmArgs = listOf(
            "--module-path", configurations.runtimeClasspath.get().asPath,
            "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics",
            "--enable-native-access=ALL-UNNAMED"
    )
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("CubicLauncher")
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
                "Main-Class" to "com.cubiclauncher.launcher.Launcher"
        )
    }

    from(sourceSets.main.get().output)

    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}