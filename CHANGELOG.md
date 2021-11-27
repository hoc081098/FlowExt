## 0.2.0-SNAPSHOT

-   Update
    -   `Kotlin` to `1.6.0`.
    -   `KotlinX Coroutines` to `1.6.0-RC`.

-   Do not propagate cancellation to the upstream in Flow `flatMapFirst` operators
    (Related to https://github.com/Kotlin/kotlinx.coroutines/pull/2964).

-   Remove unnecessary `@ExperimentalCoroutinesApi`.

-   Rename `NULL_Value` to `NullValue`.

-   Add `Flow.mapIndexed`.

-   Add `Flow.retryWhenWithDelayStrategy`, `Flow.retryWhenWithExponentialBackoff`, `Flow.retryWithExponentialBackoff`.

## 0.1.0 - Nov 13, 2021

-   Update
    -   `Kotlin` to `1.5.31`.
    -   `KotlinX Coroutines` to `1.5.2`.
    -   `Gradle` to `7.3`.
-   Add
    -   `bufferCount`.
    -   `Event`.
    -   `concat`.
    -   `concatWith`.
    -   `startWith`.
    -   `interval`.
    -   `mapTo`.
    -   `materialize`.
    -   `dematerialize`.
    -   `neverFlow`.
    -   `NULL_Value`.
-   Add docs and docs site.
-   Internal bug fixes.

## 0.0.1 -> 0.0.6 - Jul 13, 2021

-   Test for Publishing.
