# Change Log

## [Unreleased] - TODO

### Changed

- Update dependencies
  - `Kotlin` to `1.9.20`.

### Removed

- Remove now-unsupported targets: `iosArm32`, `watchosX86`.

## [0.7.3] - Oct 29, 2023

### Changed

- Update dependencies
  - `Kotlin` to `1.9.10`.
  - `Gradle` to `8.4`.

- Annotate `Symbol` and `NULL_VALUE` with `@DelicateFlowExtApi`.

### Added

- Add `Flow.chunked` operator, it is an alias to `Flow.bufferCount` operator.
- Add `Flow.pairwise(transform)` operator - a variant of `Flow.pairwise()` operator,
  which allows the transformation of the pair of values via the `transform` lambda parameter.

- Add `Flow.zipWithNext()` operator, it is an alias to `Flow.pairwise()` operator.
- Add `Flow.zipWithNext(transform)` operator, it is an alias to `Flow.pairwise(transform)` operator.

## [0.7.2] - Oct 7, 2023

### Changed

- Update dependencies
  - `Gradle` to `8.3`.

### Added

- Add `Flow.ignoreElements` operator.
- Add `Flow.scanWith` operator.
- Add `Flow.safeCast` operator (thanks to [@hoangchungk53qx1](https://github.com/hoangchungk53qx1)).

### Fixed

- `Flow.select`: avoid calling sub-selectors when the previous state is the same as the current state
  (aka. `distinctUntilChanged`).

## [0.7.0] ~ [0.7.1] - Jul 31, 2023

### Changed

- Update dependencies
  - `Kotlin` to `1.9.0`.
  - `KotlinX Coroutines` to `1.7.3`.
  - `Gradle` to `8.2`.

### Added

- Add `Flow.repeat` operator.

## [0.6.1] - May 18, 2023

### Changed

- Update dependencies
  - `Kotlin` to `1.8.21`.
  - `KotlinX Coroutines` to `1.7.1`.
  - `Gradle` to `8.1.1`.

## [0.6.0] - Mar 28, 2023

### Changed

- Update dependencies
  - `Kotlin` to `1.8.10`.
  - `KotlinX Coroutines` to `1.7.0-Beta`.
  - `Gradle` to `8.0.2`.

- Only support JS IR.
- Supports more targets:
  - `linuxArm64`
  - `watchosDeviceArm64`
  - `androidNativeArm32`
  - `androidNativeArm64`
  - `androidNativeX86`
  - `androidNativeX64`

### Added

- [`Flow.groupBy`](https://hoc081098.github.io/FlowExt/docs/0.x/-flow-ext/com.hoc081098.flowext/groupBy.html) operator.
  See [Readme#groupBy](https://github.com/hoc081098/FlowExt#groupBy) for more details.

## [0.5.0] - Nov 7, 2022

### Changed

- Update dependencies
  - `Kotlin` to `1.7.20` (The new Kotlin/Native memory manager enabled by default).
  - `Gradle` to `7.5.1`.

- Remove unnecessary `@ExperimentalCoroutinesApi` and `@ExperimentalTime` on `skipUntil`/`dropUntil`.

### Added

- [`Flow.select`](https://hoc081098.github.io/FlowExt/docs/0.x/-flow-ext/com.hoc081098.flowext/select.html) operator (from `1` to `5`).
  See [Readme#select](https://github.com/hoc081098/FlowExt#select) for more details.

## [0.4.0] - Jul 22, 2022

- Update
  - `KotlinX Coroutines` to `1.6.4`.
  - `Gradle` to `7.5`.

- Add
  - `defer`.
  - `flowFromSuspend`.
  - `mapEager`, `flatMapConcatEager`, `flattenConcatEager`.
  - `skipUntil`, `dropUntil`.
  - `pairwise`.
  - `NeverFlow` interface and `NeverFlow.Companion` object.
  - `cast`, `castNotNull`, `castNullable`.
  - `combine` versions for `6 - 12` `Flow`s.

- Refactor
  - `neverFlow()` now returns `NeverFlow`.
  - `takeUntil`: change `notifier`'s type to `Flow<Any?>`

- Internal fix for `AtomicRef`: freeze `value` if `AtomicRef` is frozen.

- Support for Apple Silicon targets
  - `iosSimulatorArm64`.
  - `macosArm64`.
  - `tvosSimulatorArm64`.
  - `watchosSimulatorArm64`.

- Enable compatibility with non-hierarchical multiplatform projects.

## [0.3.0] - May 2, 2022

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

## [0.2.0] - Jan 3, 2022

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

## [0.1.0] - Nov 13, 2021

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

## [0.0.1] -> [0.0.6] - Jul 13, 2021

- Test for Publishing.

[Unreleased]: https://github.com/hoc081098/FlowExt/compare/0.7.3...HEAD
[0.7.3]: https://github.com/hoc081098/FlowExt/releases/tag/0.7.3
[0.7.2]: https://github.com/hoc081098/FlowExt/releases/tag/0.7.2
[0.7.1]: https://github.com/hoc081098/FlowExt/releases/tag/0.7.1
[0.7.0]: https://github.com/hoc081098/FlowExt/releases/tag/0.7.0
[0.6.1]: https://github.com/hoc081098/FlowExt/releases/tag/0.6.1
[0.6.0]: https://github.com/hoc081098/FlowExt/releases/tag/0.6.0
[0.5.0]: https://github.com/hoc081098/FlowExt/releases/tag/0.5.0
[0.4.0]: https://github.com/hoc081098/FlowExt/releases/tag/0.4.0
[0.3.0]: https://github.com/hoc081098/FlowExt/releases/tag/0.3.0
[0.2.0]: https://github.com/hoc081098/FlowExt/releases/tag/0.2.0
[0.1.0]: https://github.com/hoc081098/FlowExt/releases/tag/0.1.0
[0.0.6]: https://github.com/hoc081098/FlowExt/releases/tag/0.0.6
[0.0.1]: https://github.com/hoc081098/FlowExt/releases/tag/0.0.1
