## 0.4.0

- Update
  - `Kotlin` to `1.7.0`.
  - `KotlinX Coroutines` to `1.6.3`.

- Add
  - `defer`.
  - `flowFromSuspend`.
  - `mapEager`, `flatMapConcatEager`, `flattenConcatEager`.
  - `skipUntil`, `dropUntil`.

- Support for Apple Silicon targets
  - `iosSimulatorArm64`.
  - `macosArm64`.
  - `tvosSimulatorArm64`.
  - `watchosSimulatorArm64`.

- Enable compatibility with non-hierarchical multiplatform projects.

## 0.3.0 - May 2, 2022

- Update
  - `Kotlin` to `1.6.21`.
  - `KotlinX Coroutines` to `1.6.1`.
  - `Gradle` to `7.4.2`.

- Refactor `withLatestFrom`'s implementation.

- Move `NULL_VALUE` to `com.hoc081098.flowext.utils` package.

- Add `Symbol` class.

- Add
  - `Flow.throttleTime`.
  - `Event.flatMap`.
  - `Event.valueOrDefault`.
  - `Event.valueOrElse`.
  - `race`, `amb`.
  - `Flow.raceWith`, `Flow.ambWith`.
  - `Flow.mapToUnit`.
  - `Flow.startWith { }` that accepts a lambda parameter.

- Add and update docs.

- Add more test cases, increase code coverage.

- Internal bug fixes.

## 0.2.0 - Jan 3, 2022

- Update
  - `Kotlin` to `1.6.10`.
  - `KotlinX Coroutines` to `1.6.0`.
  - `Gradle` to `7.3.3`.

- Do not propagate cancellation to the upstream in Flow `flatMapFirst` operators
  (Related to https://github.com/Kotlin/kotlinx.coroutines/pull/2964).

- Remove unnecessary `@ExperimentalCoroutinesApi`s, `@ExperimentalTime`s.

- Rename `NULL_Value` to `NULL_VALUE`.

- Add `Flow.mapIndexed`.

- Add
  - `DelayStrategy`.
  - `Flow.retryWhenWithDelayStrategy`.
  - `Flow.retryWhenWithExponentialBackoff`.
  - `Flow.retryWithExponentialBackoff`.

## 0.1.0 - Nov 13, 2021

- Update
  - `Kotlin` to `1.5.31`.
  - `KotlinX Coroutines` to `1.5.2`.
  - `Gradle` to `7.3`.
- Add
  - `bufferCount`.
  - `Event`.
  - `concat`.
  - `concatWith`.
  - `startWith`.
  - `interval`.
  - `mapTo`.
  - `materialize`.
  - `dematerialize`.
  - `neverFlow`.
  - `NULL_Value`.
- Add docs and docs site.
- Internal bug fixes.

## 0.0.1 -> 0.0.6 - Jul 13, 2021

- Test for Publishing.
