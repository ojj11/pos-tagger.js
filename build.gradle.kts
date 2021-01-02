plugins {
    kotlin("multiplatform") version "1.4.20"
    kotlin("plugin.serialization") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

repositories {
    mavenCentral()
    jcenter()
}

kotlin {

    js(IR) {
        useCommonJs()
        nodejs()
        binaries.executable()
    }

    jvm()

    sourceSets {

        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.0.1")
            }
        }

        get("jsMain").dependencies {
            implementation(kotlin("stdlib-js"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.0.1")
        }

        get("jvmMain").dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.0.1")
        }
    }
}

val convertModels by tasks.creating(JavaExec::class) {
    group = "application"
    description = "Convert original models to new CBOR format"
    main = "com.github.ojj11.ConvertModels"
    kotlin {
        val main = targets["jvm"].compilations["main"]
        dependsOn(main.compileAllTaskName)
        classpath(
            { main.output.allOutputs.files },
            { configurations["jvmRuntimeClasspath"] }
        )
    }
    // /disable app icon on macOS
    systemProperty("java.awt.headless", "true")
}
