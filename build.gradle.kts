plugins {
    kotlin("multiplatform") version "1.4-M3"
    kotlin("plugin.serialization") version "1.4-M3"
}

repositories {
    mavenCentral()
    maven ("https://dl.bintray.com/kotlin/kotlin-eap")
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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.20.0")
            }
        }

        get("jsMain").dependencies {
            implementation(kotlin("stdlib-js"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0-1.4-M3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor-js:0.20.0-1.4-M3")

        }

        get("jvmMain").dependencies {
            implementation(kotlin("stdlib-jdk8"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:0.20.0-1.4-M3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0-1.4-M3")
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
    ///disable app icon on macOS
    systemProperty("java.awt.headless", "true")
}
