import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishBasePlugin
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import java.net.URL

plugins {
  kotlin("multiplatform") version "1.9.23"
  id("com.diffplug.spotless") version "6.25.0"
  id("maven-publish")
  id("com.vanniktech.maven.publish") version "0.28.0"
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.15.0-Beta.1"
  id("org.jetbrains.dokka") version "1.9.20"
  id("org.jetbrains.kotlinx.kover") version "0.7.6"
}

val coroutinesVersion = "1.8.1-Beta"
val ktlintVersion = "1.0.0"

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
}

apiValidation {
  @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
  klib {
    enabled = true
  }
}

kotlin {
  explicitApi()

  sourceSets {
    all {
      languageSettings {
        optIn("com.hoc081098.flowext.DelicateFlowExtApi")
      }
    }
  }

  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
    vendor.set(JvmVendorSpec.AZUL)
  }

  jvm {
    compilations.all {
      compilerOptions.configure {
        jvmTarget = JvmTarget.JVM_1_8
      }
    }
  }

  js(IR) {
    moduleName = project.name
    compilations.configureEach {
      compilerOptions.configure {
        sourceMap.set(true)
        moduleKind.set(JsModuleKind.MODULE_COMMONJS)
      }
    }

    browser {
      testTask {
        useMocha {
          timeout = "10s"
        }
      }
    }
    nodejs {
      testTask {
        useMocha {
          timeout = "10s"
        }
      }
    }
  }

  // If false - WASM targets will not be configured in multiplatform projects.
  val kmpWasmEnabled =
    System.getProperty("kwasm", "true")
      .toBoolean()
      .also { println(">>> kmpWasmEnabled=$it") }

  if (kmpWasmEnabled) {
    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
    wasmJs {
      // Module name should be different from the one from JS
      // otherwise IC tasks that start clashing different modules with the same module name
      moduleName = project.name + "Wasm"

      browser {
        testTask {
          useMocha {
            timeout = "10s"
          }
        }
      }
      nodejs {
        testTask {
          useMocha {
            timeout = "10s"
          }
        }
      }
    }
  }

  // According to https://kotlinlang.org/docs/native-target-support.html

  iosArm64()
  iosX64()
  iosSimulatorArm64()

  macosX64()
  macosArm64()
  mingwX64()
  linuxX64()
  linuxArm64()

  tvosX64()
  tvosSimulatorArm64()
  tvosArm64()

  watchosArm32()
  watchosArm64()
  watchosX64()
  watchosSimulatorArm64()
  watchosDeviceArm64()

  androidNativeArm32()
  androidNativeArm64()
  androidNativeX86()
  androidNativeX64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
      }
    }

    jvmTest {
      dependencies {
        implementation(kotlin("test-junit"))
      }
    }

    val nonJvmMain by creating {
      dependsOn(commonMain.get())
    }
    val nonJvmTest by creating {
      dependsOn(commonTest.get())
    }

    val jsAndWasmMain by creating {
      dependsOn(nonJvmMain)
    }
    val jsAndWasmTest by creating {
      dependsOn(nonJvmTest)
    }

    jsMain {
      dependsOn(jsAndWasmMain)
    }
    jsTest {
      dependsOn(jsAndWasmTest)
      dependencies {
        implementation(kotlin("test-js"))
      }
    }

    if (kmpWasmEnabled) {
      val wasmJsMain by getting {
        dependsOn(jsAndWasmMain)
      }
      val wasmJsTest by getting {
        dependsOn(jsAndWasmTest)
        dependencies {
          implementation(kotlin("test-wasm-js"))
        }
      }
    }

    nativeMain {
      dependsOn(nonJvmMain)
    }
    nativeTest {
      dependsOn(nonJvmTest)
    }
  }

  // enable running ios tests on a background thread as well
  // configuration copied from: https://github.com/square/okio/pull/929
  targets.withType<KotlinNativeTargetWithTests<*>>().all {
    binaries {
      // Configure a separate test where code runs in background
      test("background", setOf(NativeBuildType.DEBUG)) {
        freeCompilerArgs = freeCompilerArgs + "-trw"
      }
    }
    testRuns {
      val background by creating {
        setExecutionSourceFrom(binaries.getTest("background", NativeBuildType.DEBUG))
      }
    }
  }
}

tasks.withType<KotlinCompile<*>>().configureEach {
  kotlinOptions {
    // 'expect'/'actual' classes (including interfaces, objects, annotations, enums,
    // and 'actual' typealiases) are in Beta.
    // You can use -Xexpect-actual-classes flag to suppress this warning.
    // Also see: https://youtrack.jetbrains.com/issue/KT-61573
    freeCompilerArgs +=
      listOf(
        "-Xexpect-actual-classes",
      )
  }
}

spotless {
  kotlin {
    target("**/*.kt")

    ktlint(ktlintVersion)
      .setEditorConfigPath("$rootDir/.editorconfig")

    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()

    licenseHeaderFile(rootProject.file("spotless/license.txt"))
  }

  kotlinGradle {
    target("**/*.kts")

    ktlint(ktlintVersion)
      .setEditorConfigPath("$rootDir/.editorconfig")

    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()
  }
}

allprojects {
  plugins.withType<MavenPublishBasePlugin> {
    extensions.configure<MavenPublishBaseExtension> {
      publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
      signAllPublications()
    }
  }
}

tasks.withType<AbstractTestTask> {
  testLogging {
    showExceptions = true
    showCauses = true
    showStackTraces = true
    showStandardStreams = true
    events =
      setOf(
        TestLogEvent.PASSED,
        TestLogEvent.FAILED,
        TestLogEvent.SKIPPED,
        TestLogEvent.STANDARD_OUT,
        TestLogEvent.STANDARD_ERROR,
      )
    exceptionFormat = TestExceptionFormat.FULL
  }
}

tasks.withType<DokkaTask>().configureEach {
  dokkaSourceSets {
    configureEach {
      externalDocumentationLink("https://kotlinlang.org/api/kotlinx.coroutines/")

      sourceLink {
        localDirectory.set(file("src"))
        remoteUrl.set(URL("https://github.com/hoc081098/FlowExt/tree/master/src"))
        remoteLineSuffix.set("#L")
      }
    }
  }
}

/**
 * [Reference](https://github.com/square/okio/blob/55b7210fb3d52de07f4bc1122c5062e38df576d9/build.gradle.kts#L227-L248).
 *
 * Select a NodeJS version with WASI and WASM GC.
 * https://github.com/Kotlin/kotlin-wasm-examples/blob/main/wasi-example/build.gradle.kts
 */
plugins.withType<NodeJsRootPlugin> {
  extensions.getByType<NodeJsRootExtension>().apply {
    if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) {
      // We're waiting for a Windows build of NodeJS that can do WASM GC + WASI.
      nodeVersion = "21.4.0"
    } else {
      // Reference:
      // https://github.com/drewhamilton/Poko/blob/72ec8d24cf48a74b3d1125c94f0e625ab956b93f/build.gradle.kts#L17-L19
      // WASM requires a canary Node.js version. This is the last v21 canary, and has both
      // darwin-arm64 and darwin-x64 artifacts:
      nodeVersion = "21.0.0-v8-canary20231024d0ddc81258"
      nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
    }
  }
  // Suppress an error because yarn doesn't like our Node version string.
  //   warning You are using Node "21.0.0-v8-canary20231024d0ddc81258" which is not supported and
  //   may encounter bugs or unexpected behavior.
  //   error karma@6.4.2: The engine "node" is incompatible with this module.
  tasks.withType<KotlinNpmInstallTask>().all {
    args += "--ignore-engines"
  }
}
