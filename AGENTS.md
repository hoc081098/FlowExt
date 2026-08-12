# Repository Guidelines

## Project Structure & Module Organization

FlowExt is a single-module Kotlin Multiplatform library, not a runnable application. Public Flow operators live in `src/commonMain/kotlin/com/hoc081098/flowext/`; shared internals are under `internal/`. Platform implementations belong in the matching source set, such as `jvmMain`, `nativeMain`, `jsAndWasmMain`, `appleMain`, or `linuxMain`. Put portable tests in `src/commonTest` and JVM-specific cases in `src/jvmTest`. The `api/FlowExt.api` and `api/FlowExt.klib.api` files are checked-in public API baselines. Project documentation is in `README.md`, with `logo.png` as the primary asset.

## Build, Test, and Development Commands

Use JDK 17 and the checked-in Gradle wrapper.

- `./gradlew build --stacktrace` compiles all available targets and runs the CI-equivalent test suite.
- `./gradlew jvmTest` runs the faster JVM/shared test subset during development.
- `./gradlew spotlessCheck` verifies formatting; `./gradlew spotlessApply` fixes it.
- `./gradlew apiCheck` validates binary API compatibility. Run `./gradlew apiDump` only after an intentional public API change and review the resulting diff.
- `./gradlew koverXmlReport` creates the coverage report; `./gradlew dokkaGenerate` builds API docs in `build/dokka/html`.

On Windows, CI disables Wasm with `./gradlew build -Dkwasm=false`.

## Coding Style & Naming Conventions

Follow `.editorconfig`: UTF-8, LF endings, two-space indentation, no trailing whitespace, and a final newline. Spotless uses ktlint's official Kotlin style and applies the MIT header to Kotlin sources. Because explicit API mode is enabled, declare public visibility and types deliberately. Use PascalCase for types, camelCase for functions, and established operator filenames such as `withLatestFrom.kt`; type-focused files may use names such as `Event.kt`.

## Testing Guidelines

Tests use `kotlin.test` and `kotlinx-coroutines-test`; JVM tests use JUnit, while JS/Wasm use Mocha. Name test files and classes `*Test` (or `*JvmTest`) and methods descriptively, usually beginning with `test`. Cover normal emission, failure propagation, cancellation, and timing behavior where relevant. No numeric coverage minimum is configured, but CI uploads Kover results.

## Commit & Pull Request Guidelines

Use a concise imperative subject, preferably Conventional Commit style, for example `fix(flow): preserve cancellation` or `docs: update installation`. Complete the PR template: status, breaking-change declaration, description, and change type. Check applicable boxes for API dumps, README/CHANGELOG updates, tests, and docs. Keep changes scoped and ensure the multi-platform build passes before requesting review.

## Public API & Release Safety

Do not commit `local.properties`, IDE caches, build output, signing keys, or Maven credentials. Follow `RELEASING.md` for version/tag work; do not run publication tasks as part of normal verification.
