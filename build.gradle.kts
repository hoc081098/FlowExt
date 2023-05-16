import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishBasePlugin
import com.vanniktech.maven.publish.SonatypeHost
import java.net.URL
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
  kotlin("multiplatform") version "1.8.21"
  id("com.diffplug.spotless") version "6.18.0"
  id("maven-publish")
  id("com.vanniktech.maven.publish") version "0.25.2"
  id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.13.1"
  id("org.jetbrains.dokka") version "1.8.10"
  id("org.jetbrains.kotlinx.kover") version "0.7.0"
}

val coroutinesVersion = "1.7.1"
val ktlintVersion = "0.46.1"

repositories {
  mavenCentral()
  google()
  gradlePluginPortal()
}

kotlin {
  explicitApi()
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
    vendor.set(JvmVendorSpec.AZUL)
  }

  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
  }
  js(IR) {
    compilations.all {
      kotlinOptions {
        sourceMap = true
        moduleKind = "umd"
        metaInfo = true
      }
    }
    browser {
      testTask {
        useMocha()
      }
    }
    nodejs {
      testTask {
        useMocha()
      }
    }
  }

  // According to https://kotlinlang.org/docs/native-target-support.html

  iosArm64()
  iosArm32()
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
  watchosX86()
  watchosSimulatorArm64()
  watchosDeviceArm64()

  androidNativeArm32()
  androidNativeArm64()
  androidNativeX86()
  androidNativeX64()

  sourceSets {
    val commonMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
      }
    }
    val jvmMain by getting {
      dependsOn(commonMain)
    }
    val jvmTest by getting {
      dependsOn(commonTest)

      dependencies {
        implementation(kotlin("test-junit"))
      }
    }
    val jsMain by getting {
      dependsOn(commonMain)
    }
    val jsTest by getting {
      dependencies {
        implementation(kotlin("test-js"))
      }
    }

    val nativeMain by creating {
      dependsOn(commonMain)
    }
    val nativeTest by creating {
      dependsOn(commonTest)
    }

    val darwinMain by creating {
      dependsOn(nativeMain)
    }
    val darwinTest by creating {
      dependsOn(nativeTest)
    }

    val linuxMain by creating {
      dependsOn(nativeMain)
    }
    val linuxTest by creating {
      dependsOn(nativeTest)
    }

    val pthreadAndroidMain by creating {
      dependsOn(nativeMain)
    }
    val pthreadAndroidTest by creating {
      dependsOn(nativeTest)
    }

    val appleTargets = listOf(
      "iosX64",
      "iosSimulatorArm64",
      "iosArm64",
      "iosArm32",
      "macosX64",
      "macosArm64",
      "tvosArm64",
      "tvosX64",
      "tvosSimulatorArm64",
      "watchosArm32",
      "watchosArm64",
      "watchosX86",
      "watchosSimulatorArm64",
      "watchosX64",
      "watchosSimulatorArm64",
      "watchosDeviceArm64"
    )

    val linuxTargets = listOf(
      "linuxX64",
      "linuxArm64"
    )

    val androidNativeTargets = listOf(
      "androidNativeArm32",
      "androidNativeArm64",
      "androidNativeX86",
      "androidNativeX64"
    )

    appleTargets.forEach {
      getByName("${it}Main") {
        dependsOn(darwinMain)
      }
      getByName("${it}Test") {
        dependsOn(darwinTest)
      }
    }

    linuxTargets.forEach {
      getByName("${it}Main") {
        dependsOn(linuxMain)
      }
      getByName("${it}Test") {
        dependsOn(linuxTest)
      }
    }

    androidNativeTargets.forEach {
      getByName("${it}Main") {
        dependsOn(pthreadAndroidMain)
      }
      getByName("${it}Test") {
        dependsOn(pthreadAndroidTest)
      }
    }

    getByName("mingwX64Main") {
      dependsOn(nativeMain)
    }
    getByName("mingwX64Test") {
      dependsOn(nativeTest)
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

spotless {
  val EDITOR_CONFIG_KEYS: Set<String> = hashSetOf(
    "ij_kotlin_imports_layout",
    "indent_size",
    "end_of_line",
    "charset",
    "continuation_indent_size",
    "disabled_rules"
  )

  kotlin {
    target("**/*.kt")

    // TODO this should all come from editorconfig https://github.com/diffplug/spotless/issues/142
    val data = mapOf(
      "indent_size" to "2",
      "continuation_indent_size" to "4",
      "ij_kotlin_imports_layout" to "*",
      "end_of_line" to "lf",
      "charset" to "utf-8",
      "disabled_rules" to arrayOf("filename").joinToString(separator = ",")
    )

    ktlint(ktlintVersion)
      .setUseExperimental(true)
      .userData(data.filterKeys { it !in EDITOR_CONFIG_KEYS })
      .editorConfigOverride(data.filterKeys { it in EDITOR_CONFIG_KEYS })

    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()

    licenseHeaderFile(rootProject.file("spotless/license.txt"))
  }

  kotlinGradle {
    target("**/*.kts")

    val data = mapOf(
      "indent_size" to "2",
      "continuation_indent_size" to "4",
      "ij_kotlin_imports_layout" to "*",
      "end_of_line" to "lf",
      "charset" to "utf-8"
    )
    ktlint(ktlintVersion)
      .setUseExperimental(true)
      .userData(data.filterKeys { it !in EDITOR_CONFIG_KEYS })
      .editorConfigOverride(data.filterKeys { it in EDITOR_CONFIG_KEYS })

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
    events = setOf(
      TestLogEvent.PASSED,
      TestLogEvent.FAILED,
      TestLogEvent.SKIPPED,
      TestLogEvent.STANDARD_OUT,
      TestLogEvent.STANDARD_ERROR
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
