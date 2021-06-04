plugins {
    kotlin("multiplatform") version "1.5.0"
    jacoco
    id("com.diffplug.spotless") version "5.12.5"
}

group = "com.hoc081098"
version = "1.0"

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = "0.8.6"
}

tasks.withType<JacocoReport> {
    reports {
        xml.isEnabled = true
    }
    dependsOn(tasks.withType<Test>())
}

val kotlinCoroutinesVersion = "1.5.0"

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
            finalizedBy(tasks.withType<JacocoReport>())
        }
    }
    js {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        nodejs()
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("app.cash.turbine:turbine:0.5.2")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting
    }
}

spotless {
    kotlin {
        target("**/*.kt")

        ktlint("0.37.2").userData(
            mapOf(
                // TODO this should all come from editorconfig https://github.com/diffplug/spotless/issues/142
                "indent_size" to "2",
                "kotlin_imports_layout" to "ascii",
            )
        )
    }
}