# FlowExt

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hoc081098/FlowExt?style=flat)](https://search.maven.org/search?q=io.github.hoc081098%20FlowExt)
[![codecov](https://codecov.io/gh/hoc081098/FlowExt/branch/master/graph/badge.svg?token=9KGcZ7P2hV)](https://codecov.io/gh/hoc081098/FlowExt)
[![Build](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml/badge.svg)](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml)
[![Validate Gradle Wrapper](https://github.com/hoc081098/FlowExt/actions/workflows/gradle-wrapper-validation.yml/badge.svg)](https://github.com/hoc081098/FlowExt/actions/workflows/gradle-wrapper-validation.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin version](https://img.shields.io/badge/Kotlin-2.0.20-blueviolet?logo=kotlin&logoColor=white)](https://github.com/JetBrains/kotlin/releases/tag/v2.0.20)
[![KotlinX Coroutines version](https://img.shields.io/badge/Kotlinx_Coroutines-1.9.0-blueviolet?logo=kotlin&logoColor=white)](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.9.0)
[![klibs.io](https://img.shields.io/badge/KLIBS_IO-blueviolet?logo=kotlin&logoColor=white)](https://klibs.io/project/hoc081098/FlowExt)
![badge][badge-android]
![badge][badge-wearos]
![badge][badge-android-native]
![badge][badge-jvm]
![badge][badge-js]
![badge][badge-js-ir]
![badge][badge-wasm]
![badge][badge-nodejs]
![badge][badge-linux]
![badge][badge-windows]
![badge][badge-ios]
![badge][badge-mac]
![badge][badge-watchos]
![badge][badge-tvos]
![badge][badge-apple-silicon]
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fhoc081098%2FFlowExt&count_bg=%2379C83D&title_bg=%23555555&icon=kotlin.svg&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)

- FlowExt is a Kotlin Multiplatform library, that provides many operators and extensions to Kotlin Coroutines Flow.
- FlowExt provides a collection of operators, Flows and utilities for Flow, that are not provided by Kotlinx Coroutine
  themselves, but are common in other Reactive Frameworks (rxjs, RxJava, RxSwift, rxdart, ...) and standards.

<p align="center">
    <img src="https://github.com/hoc081098/FlowExt/raw/master/logo.png" width="400">
</p>

<details>

> Kotlinx Coroutines Flow Extensions. Extensions to the Kotlin Flow library. Kotlin Flow extensions.
> Multiplatform Kotlinx Coroutines Flow Extensions. Multiplatform Extensions to the Kotlin Flow
> library. Multiplatform Kotlin Flow extensions. RxJS Kotlin Coroutines Flow. RxSwift Kotlin
> Coroutines Flow. RxJava Kotlin Coroutines Flow. RxJS Kotlin Flow. RxSwift Kotlin Flow. RxJava
> Kotlin
> Flow. RxJS Coroutines Flow. RxSwift Coroutines Flow. RxJava Coroutines Flow. Kotlin Flow
> operators.
> Coroutines Flow operators.

</details>

## Author: [Petrus Nguyễn Thái Học](https://github.com/hoc081098)

Liked some of my work? Buy me a coffee (or more likely a beer)

<a href="https://www.buymeacoffee.com/hoc081098" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" height=64></a>

## Supported targets

- `android`.
- `jvm`.
- `js` (`IR`).
- `wasmJs`.
- `iosArm64`, `iosX64`, `iosSimulatorArm64`.
- `watchosArm32`, `watchosArm64`, `watchosX64`, `watchosSimulatorArm64`, `watchosDeviceArm64`.
- `tvosX64`, `tvosSimulatorArm64`, `tvosArm64`.
- `macosX64`, `macosArm64`.
- `mingwX64`
- `linuxX64`, `linuxArm64`.
- `androidNativeArm32`, `androidNativeArm64`, `androidNativeX86`, `androidNativeX64`.

## API

> **Note**: I gladly accept PRs, ideas, opinions, or improvements. Thank you! :)

### 0.x release docs: https://hoc081098.github.io/FlowExt/docs/0.x

### 1.x release docs: https://hoc081098.github.io/FlowExt/docs/1.x

### Snapshot docs: https://hoc081098.github.io/FlowExt/docs/latest

## Installation

```groovy
allprojects {
  repositories {
    ...
    mavenCentral()
  }
}
```

### Multiplatform

```groovy
implementation("io.github.hoc081098:FlowExt:1.0.0")
```

### Snapshot

<details>
  <summary>Snapshots of the development version are available in Sonatype's snapshots repository.</summary>

- Kotlin

```kotlin
allprojects {
  repositories {
    ...
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}

dependencies {
  implementation("io.github.hoc081098:FlowExt:1.0.1-SNAPSHOT")
}
```

- Groovy

```groovy
allprojects {
  repositories {
    ...
    maven { url "https://s01.oss.sonatype.org/content/repositories/snapshots/" }
  }
}

dependencies {
  implementation("io.github.hoc081098:FlowExt:1.0.1-SNAPSHOT")
}
```

</details>

## Table of contents

- Create
  - [`concat`](#concat)
  - [`defer`](#defer)
  - [`flowFromNonSuspend`](#flowfromnonsuspend)
  - [`flowFromSuspend`](#flowfromsuspend)
  - [`interval`](#interval)
  - [`neverFlow`](#neverflow)
  - [`race`](#race--amb)
  - [`amb`](#race--amb)
  - [`range`](#range)
  - [`timer`](#timer)

- Intermediate operators
  - [`bufferCount`](#buffercount--chunked)
  - [`combine`](#combine)
  - [`cast`](#cast--castnotnull--castnullable--safeCast)
  - [`castNotNull`](#cast--castnotnull--castnullable--safeCast)
  - [`castNullable`](#cast--castnotnull--castnullable--safeCast)
  - [`catchAndReturn`, `catchAndResume`](#catchAndReturn--catchAndResume)
  - [`chunked`](#buffercount--chunked)
  - [`safeCast`](#cast--castnotnull--castnullable--safeCast)
  - [`concatWith`](#concatwith--plus)
  - [`startWith`](#startwith)
  - [`flatMapFirst`](#flatmapfirst--exhaustmap)
  - [`exhaustMap`](#flatmapfirst--exhaustmap)
  - [`flattenFirst`](#flattenfirst--exhaustall)
  - [`flatMapConcatEager`](#flatmapconcateager)
  - `mapEager`
  - `flattenEager`
  - [`exhaustAll`](#flattenfirst--exhaustall)
  - [`groupBy`](#groupby)
  - [`ignoreElements`](#ignoreelements)
  - [`mapIndexed`](#mapindexed)
  - [`mapTo`](#mapto)
  - [`mapToUnit`](#maptounit)
  - [`mapToResult`](#mapToResult)
  - [`mapResultCatching`](#mapResultCatching)
  - [`throwFailure`](#throwFailure)
  - [`materialize`](#materialize)
  - [`dematerialize`](#dematerialize)
  - [`raceWith`](#racewith--ambwith)
  - [`ambWith`](#racewith--ambwith)
  - [`pairwise`](#pairwise--zipWithNext)
  - [`repeat`](#repeat)
  - [`retryWhenWithDelayStrategy`](#retrywhenwithdelaystrategy)
  - [`retryWhenWithExponentialBackoff`](#retrywhenwithexponentialbackoff)
  - [`retryWithExponentialBackoff`](#retrywithexponentialbackoff)
  - [`scanWith`](#scanWith)
  - [`select`](#select)
  - [`skipUntil`](#skipuntil--dropuntil)
  - [`dropUntil`](#skipuntil--dropuntil)
  - [`takeUntil`](#takeuntil)
  - [`throttleTime`](#throttletime)
  - [`withLatestFrom`](#withlatestfrom)
  - [`zipWithNext`](#pairwise--zipWithNext)
  - [`plus`](#concatwith--plus)

#### bufferCount / chunked

- Similar to [RxJS bufferCount](https://rxjs.dev/api/operators/bufferCount)
- Similar
  to [RxJava buffer](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#buffer-int-int-)

Buffers the source `Flow` values until the size hits the maximum `bufferSize` given.

Note, `chunked` is an alias to `bufferCount`.

```kotlin
range(start = 0, count = 10)
  .bufferCount(bufferSize = 3)
  .collect { println("bufferCount: $it") }

println("---")

range(start = 0, count = 10)
  .bufferCount(bufferSize = 3, startBufferEvery = 2)
  .collect { println("bufferCount: $it") }
```

Output:

```none
bufferCount: [0, 1, 2]
bufferCount: [3, 4, 5]
bufferCount: [6, 7, 8]
bufferCount: [9]
---
bufferCount: [0, 1, 2]
bufferCount: [2, 3, 4]
bufferCount: [4, 5, 6]
bufferCount: [6, 7, 8]
bufferCount: [8, 9]
```

----

#### concat

- Similar to [RxJS concat](https://rxjs.dev/api/index/function/concat)
- Similar
  to [RxJava concat](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#concat-java.lang.Iterable-)

Creates an output `Flow` which sequentially emits all values from the first given `Flow` and then moves on to the next.

```kotlin
concat(
  flow1 = flowOf(1, 2, 3),
  flow2 = flowOf(4, 5, 6)
).collect { println("concat: $it") }
```

Output:

```none
concat: 1
concat: 2
concat: 3
concat: 4
concat: 5
concat: 6
```

----

#### defer

- Similar to [RxJS defer](https://rxjs.dev/api/index/function/defer)
- Similar
  to [RxJava defer](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#defer-io.reactivex.rxjava3.functions.Supplier-)

Creates a `Flow` that, on collection, calls a `Flow` factory to make a `Flow` for each new `FlowCollector`.
In some circumstances, waiting until the last minute (that is, until collection time)
to generate the `Flow` can ensure that collectors receive the freshest data.

```kotlin
var count = 0L
val flow = defer {
  delay(count)
  flowOf(count++)
}

flow.collect { println("defer: $it") }
println("---")
flow.collect { println("defer: $it") }
println("---")
flow.collect { println("defer: $it") }
```

Output:

```none
defer: 0
---
defer: 1
---
defer: 2
```

----

#### flowFromNonSuspend

- Similar
  to [RxJava fromCallable](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#fromCallable-java.util.concurrent.Callable-)

Creates a _cold_ flow that produces a single value from the given `function`.
It calls the function for each new `FlowCollector`.

See also [flowFromSuspend](#flowFromSuspend) for the suspend version.

```kotlin
var count = 0L
val flow = flowFromNonSuspend { count++ }

flow.collect { println("flowFromNonSuspend: $it") }
println("---")
flow.collect { println("flowFromNonSuspend: $it") }
println("---")
flow.collect { println("flowFromNonSuspend: $it") }
```

Output:

```none
flowFromNonSuspend: 0
---
flowFromNonSuspend: 1
---
flowFromNonSuspend: 2
```

#### flowFromSuspend

- Similar
  to [RxJava fromCallable](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#fromCallable-java.util.concurrent.Callable-)

Creates a _cold_ flow that produces a single value from the given `function`.
It calls the function for each new `FlowCollector`.

See also [flowFromNonSuspend](#flowFromNonSuspend) for the non-suspend version.

```kotlin
var count = 0L
val flow = flowFromSuspend {
  delay(count)
  count++
}

flow.collect { println("flowFromSuspend: $it") }
println("---")
flow.collect { println("flowFromSuspend: $it") }
println("---")
flow.collect { println("flowFromSuspend: $it") }
```

Output:

```none
flowFromSuspend: 0
---
flowFromSuspend: 1
---
flowFromSuspend: 2
```

----

#### interval

- Similar to [RxJS interval](https://rxjs.dev/api/index/function/interval)

Returns a `Flow` that emits a `0L` after the `initialDelay` and ever-increasing numbers
after each `period` of time thereafter.

```kotlin
interval(initialDelay = 100.milliseconds, period = 1.seconds)
  .take(5)
  .collect { println("interval: $it") }
```

Output:

```none
interval: 0
interval: 1
interval: 2
interval: 3
interval: 4
```

----

#### neverFlow

- Similar to [RxJS NEVER](https://rxjs.dev/api/index/const/NEVER)

Returns a `NeverFlow` that never emits any values to the `FlowCollector` and never completes.

```kotlin
neverFlow()
  .startWith(7)
  .collect { println("neverFlow: $it") }

println("Completed!")
```

Output:

```none
neverFlow: 7
// Never prints "Completed!"
```

----

#### race / amb

- ReactiveX docs: http://reactivex.io/documentation/operators/amb.html
- Similar
  to [RxJava amb](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#amb-java.lang.Iterable-)
  .
- Similar to [RxJS race](https://rxjs.dev/api/index/function/race)

Mirrors the one `Flow` in an `Iterable` of several `Flow`s that first either emits a value
or sends a termination event (error or complete event).

When you pass a number of source `Flow`s to `race`, it will pass through the emissions
and events of exactly one of these `Flow`s: the first one that sends an event to `race`,
either by emitting a value or sending an error or complete event.
`race` will cancel the emissions and events of all of the other source `Flow`s.

```kotlin
race(
  flow {
    delay(100)
    emit(1)
    emit(2)
    emit(3)
  },
  flow {
    delay(200)
    emit(2)
    emit(3)
    emit(4)
  }
).collect { println("race: $it") }
```

Output:

```none
race: 1
race: 2
race: 3
```

----

#### range

- ReactiveX docs: http://reactivex.io/documentation/operators/range.html
- Similar to [RxJS range](https://rxjs.dev/api/index/function/range)

Creates a `Flow` that emits a sequence of numbers within a specified range.

```kotlin
range(start = 0, count = 5)
  .collect { println("range: $it") }
```

Output:

```none
range: 1
range: 2
range: 3
range: 4
```

----

#### timer

- ReactiveX docs: http://reactivex.io/documentation/operators/timer.html
- Similar to [RxJS timer](https://rxjs.dev/api/index/function/timer)

Creates a `Flow` that will wait for a given `duration`, before emitting the `value`.

```kotlin
timer(value = Unit, duration = 1.seconds)
  .collect { println("timer: $it") }
```

Output:

```none
// After 1 second
timer: kotlin.Unit
```

----

#### combine

- ReactiveX docs: https://reactivex.io/documentation/operators/combinelatest.html
- `combine` versions for `6 - 12` `Flow`s.

----

#### cast / castNotNull / castNullable / safeCast

- Similar
  to [RxJava cast](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#cast-java.lang.Class-)

----

#### catchAndReturn / catchAndResume

- Catches exceptions in the flow completion, and emits a single item or resumes with another flow.

- Similar to
  - [RxJava onErrorReturn](https://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#onErrorReturn-io.reactivex.rxjava3.functions.Function-)
  - [RxJava onErrorReturnItem](https://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#onErrorReturnItem-T-)
  - [RxJava onErrorResumeNext](https://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#onErrorResumeNext-io.reactivex.rxjava3.functions.Function-)
  - [RxJava onErrorResumeWith](https://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#onErrorResumeWith-org.reactivestreams.Publisher-)
  - [RxSwift catchAndReturn](https://github.com/ReactiveX/RxSwift/blob/551639293147e54fddced6f967a60d115818e18e/RxSwift/Observables/Catch.swift#L46)

```kotlin
flowOf(1, 2)
  .concatWith(flow { throw RuntimeException("original error") })
  .catchAndReturn(3)
  .collect { v: Int -> println("catchAndReturn: $v") }

println("---")

flowOf(1, 2)
  .concatWith(flow { throw RuntimeException("original error") })
  .catchAndReturn { e: Throwable -> e.message?.length ?: 0 }
  .collect { v: Int -> println("catchAndReturn: $v") }

println("---")

flowOf(1, 2)
  .concatWith(flow { throw RuntimeException("original error") })
  .catchAndResume(flowOf(3, 4))
  .collect { v: Int -> println("catchAndResume: $v") }

println("---")

flowOf(1, 2)
  .concatWith(flow { throw RuntimeException("original error") })
  .catchAndResume { e: Throwable -> flowOf(e.message?.length ?: 0) }
  .collect { v: Int -> println("catchAndResume: $v") }
```

Output:

```none
catchAndReturn: 1
catchAndReturn: 2
catchAndReturn: 3
---
catchAndReturn: 1
catchAndReturn: 2
catchAndReturn: 14
---
catchAndResume: 1
catchAndResume: 2
catchAndResume: 3
catchAndResume: 4
---
catchAndResume: 1
catchAndResume: 2
catchAndResume: 14
```

----

##### cast
Adapt this `Flow` to be a `Flow<R>`.

This `Flow` is wrapped as a `Flow<R>` which checks at run-time that each value event emitted
by this Flow is also an instance of `R`.

At the collection time, if this `Flow` has any value that is not an instance of `R`,
a `ClassCastException` will be thrown.

```kotlin
flowOf<Any?>(1, 2, 3)
  .cast<Int>()
  .collect { v: Int -> println("cast: $v") }
```

Output:

```none
cast: 1
cast: 2
cast: 3
```

----

##### castNotNull

Adapt this `Flow<T?>` to be a `Flow<T>`.

At the collection time, if this `Flow` has any `null` value,
a `NullPointerException` will be thrown.

```kotlin
flowOf<Int?>(1, 2, 3)
  .castNotNull()
  .collect { v: Int -> println("castNotNull: $v") }
```

Output:

```none
castNotNull: 1
castNotNull: 2
castNotNull: 3
```

----


##### safeCast

Adapt this `Flow<*>` to be a `Flow<R?>`.

At the collection time, if this `Flow` has any value that is not an instance of R, null will be emitted.

```kotlin
flowOf<Any?>(1, 2, 3, "Kotlin", null)
  .safeCast<Int?>()
  .collect { v: Int? -> println("safeCast: $v") }
```

Output:

```none
safeCast: 1
safeCast: 2
safeCast: 3
safeCast: null
safeCast: null
```

----

#### concatWith / plus

- Similar to [RxJS concatWith](https://rxjs.dev/api/operators/concatWith)
- Similar
  to [RxJava concatWith](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#concatWith-org.reactivestreams.Publisher-)

Returns a `Flow` that emits the items emitted from the current `Flow`, then the next, one after the other, without interleaving them.

Note, `plus` is an alias to `concatWith`.

```kotlin
flowOf(1, 2, 3)
  .concatWith(flowOf(4, 5, 6))
  .collect { println("concatWith: $it") }

println("---")

val flow1 = flowOf(1, 2, 3)
val flow2 = flowOf(4, 5, 6)

(flow1 + flow2).collect { println("plus: $it") }
```

Output:

```none
concatWith: 1
concatWith: 2
concatWith: 3
concatWith: 4
concatWith: 5
concatWith: 6
---
plus: 1
plus: 2
plus: 3
plus: 4
plus: 5
plus: 6
```

----

#### startWith

- Similar to [RxJS startWith](https://rxjs.dev/api/operators/startWith)
- Similar
  to [RxJava startWith](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#startWithItem-T-)

Returns a `Flow` that emits a specified item (or many items) before it begins to emit items emitted by the current `Flow`.

```kotlin
flowOf(1, 2, 3)
  .startWith(0)
  .collect { println("startWith: $i") }
```

Output:

```none
startWith: 0
startWith: 1
startWith: 2
startWith: 3
```

----

#### flatMapFirst / exhaustMap

- Similar to [RxJS exhaustMap](https://rxjs.dev/api/operators/exhaustMap)
- Similar
  to [RxSwift flatMapFirst](https://github.com/ReactiveX/RxSwift/blob/b48f2e9536cd985d912126709b97bd743e58c8fc/RxSwift/Observables/Merge.swift#L37)

Projects each source value to a `Flow` which is merged in the output `Flow` only if the previous projected `Flow` has completed.
If value is received while there is some projected `Flow` sequence being merged, it will simply be ignored.

This method is a shortcut for `map(transform).flattenFirst()`.

```kotlin
range(1, 5)
  .onEach { delay(100) }
  .flatMapFirst { timer(it, 130) }
  .collect { println("flatMapFirst: $it") }
```

Output:

```none
flatMapFirst: 1
flatMapFirst: 3
flatMapFirst: 5
```

----

#### flattenFirst / exhaustAll

- Similar to [RxJS exhaustAll](https://rxjs.dev/api/operators/exhaustAll)

Converts a higher-order `Flow` into a first-order `Flow` by dropping inner `Flow` while the previous inner `Flow` has not yet completed.

```kotlin
range(1, 5)
  .onEach { delay(100) }
  .map { timer(it, 130) }
  .flattenFirst()
  .collect { println("flattenFirst: $it") }
```

Output:

```none
flattenFirst: 1
flattenFirst: 3
flattenFirst: 5
```

----

#### groupBy

- Similar
  to [RxJava groupBy](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#groupBy-io.reactivex.rxjava3.functions.Function-)

Groups the items emitted by the current `Flow` according to a specified criterion,
and emits these grouped items as `GroupedFlow`s.

```kotlin
range(1, 10)
  .groupBy { it % 2 }
  .flatMapMerge { groupedFlow ->
    groupedFlow
      .map { groupedFlow.key to it }
  }
  .collect { println("groupBy: $it") }
```

Output:

```none
groupBy: (1, 1)
groupBy: (0, 2)
groupBy: (1, 3)
groupBy: (0, 4)
groupBy: (1, 5)
groupBy: (0, 6)
groupBy: (1, 7)
groupBy: (0, 8)
groupBy: (1, 9)
groupBy: (0, 10)
```

----

#### ignoreElements

- Similar to [RxJS ignoreElements](https://rxjs.dev/api/index/function/ignoreElements)

Ignores all elements emitted by the source `Flow`, only passes calls of `complete` or `error`.

```kotlin
flowOf("you", "talking", "to", "me")
  .ignoreElements()
  .materialize()
  .collect { println("ignoreElements: $it") }
```

Output:

```none
ignoreElements: Event.Complete
```

----

#### flatMapConcatEager

- Similar
  to [RxJava concatMapEager](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#concatMapEager-io.reactivex.rxjava3.functions.Function-)

Transforms elements emitted by the original `Flow` by applying `transform`, that returns another `flow`,
and then merging and flattening these flows.

This operator calls `transform` *sequentially* and then concatenates the resulting flows with a `concurrency`
limit on the number of concurrently collected flows.
It is a shortcut for `map(transform).flattenConcatEager(concurrency)`.

```kotlin
range(1, 5)
  .onEach { delay(100) }
  .flatMapConcatEager(concurrency = 2) { v ->
    timer(v, 130)
      .onStart { println("flatMapConcatEager: onStart $v") }
      .onCompletion { println("flatMapConcatEager: onCompletion $v") }
  }
  .collect { println("flatMapConcatEager: $it") }
```

Output:

```none
flatMapConcatEager: onStart 1
flatMapConcatEager: onStart 2
flatMapConcatEager: 1
flatMapConcatEager: onCompletion 1
flatMapConcatEager: onStart 3
flatMapConcatEager: 2
flatMapConcatEager: onCompletion 2
flatMapConcatEager: onStart 4
flatMapConcatEager: 3
flatMapConcatEager: onCompletion 3
flatMapConcatEager: onStart 5
flatMapConcatEager: 4
flatMapConcatEager: onCompletion 4
flatMapConcatEager: 5
flatMapConcatEager: onCompletion 5
```

----

#### mapIndexed

Returns a flow containing the results of applying the given `transform` function
to each value and its index in the original flow.

```kotlin
range(1, 3)
  .mapIndexed { index, value -> index to value }
  .collect { println("mapIndexed: $it") }
```

Output:

```none
mapIndexed: (0, 1)
mapIndexed: (1, 2)
mapIndexed: (2, 3)
```

----

#### mapTo

- Similar to [RxJS mapTo](https://rxjs.dev/api/operators/mapTo)

Emits the given constant value on the output `Flow` every time the source `Flow` emits a value.

```kotlin
range(1, 3)
  .mapTo("Value")
  .collect { println("mapTo: $it") }
```

Output:

```none
mapTo: Value
mapTo: Value
mapTo: Value
```

----

#### mapToUnit

Emits `kotlin.Unit` value on the output `Flow` every time the source `Flow` emits a value.

```kotlin
range(1, 3)
  .mapToUnit()
  .collect { println("mapToUnit: $it") }
```

Output:

```none
mapToUnit: kotlin.Unit
mapToUnit: kotlin.Unit
mapToUnit: kotlin.Unit
```

----

#### mapToResult

Maps values in the `Flow` to successful results (aka `Result.success`),
and catches and wraps any exception into a failure result (aka `Result.failure`).

```kotlin
flowOf(1, 2)
  .concatWith(flow { throw RuntimeException("error") })
  .mapToResult()
  .collect { result: Result<Int> -> println("mapToResult: $result") }
```

Output:

```none
mapToResult: Success(1)
mapToResult: Success(2)
mapToResult: Failure(java.lang.RuntimeException: error)
```

----

#### mapResultCatching

Maps a `Flow` of `Result`s to a `Flow` of a mapped `Result`s.
Any exception thrown by the `transform` function is caught,
and emitted as a failure result] (aka `Result.failure`) to the resulting flow.

```kotlin
flowOf(1, 2)
  .concatWith(flow { throw RuntimeException("original error") })
  .mapToResult()
  .mapResultCatching {
    if (it == 1) throw RuntimeException("another error")
    else (it * 2).toString()
  }
  .collect { result: Result<String> -> println("mapResultCatching: $result") }
```

Output:

```none
mapResultCatching: Failure(java.lang.RuntimeException: another error)
mapResultCatching: Success(4)
mapResultCatching: Failure(java.lang.RuntimeException: original error)
```

----

#### throwFailure

Maps a `Flow` of `Result`s to a `Flow` of values from successful results.
Failure results are re-thrown as exceptions.

```kotlin
try {
  flowOf(1, 2)
    .concatWith(flow { throw RuntimeException("original error") })
    .mapToResult()
    .throwFailure()
    .collect { v: Int -> println("throwFailure: $v") }
} catch (e: Throwable) {
  println("throwFailure: caught $e")
}
```

Output:

```none
throwFailure: 1
throwFailure: 2
throwFailure: caught java.lang.RuntimeException: original error
```

----

#### materialize

- Similar to [RxJS materialize](https://rxjs.dev/api/operators/materialize)
- Similar
  to [RxJava materialize](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#materialize--)

Represents all of the notifications from the source `Flow` as `value` emissions marked with their original types within `Event` objects.

```kotlin
flowOf(1, 2, 3)
  .materialize()
  .collect { println("materialize: $it") }
```

Output:

```none
materialize: Event.Value(1)
materialize: Event.Value(2)
materialize: Event.Value(3)
materialize: Event.Complete
```

----

#### dematerialize

- Similar to [RxJS dematerialize](https://rxjs.dev/api/operators/dematerialize)
- Similar
  to [RxJava dematerialize](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#dematerialize--)

Converts a `Flow` of `Event` objects into the emissions that they represent.


```kotlin
flowOf(Event.Value(1), Event.Value(2), Event.Value(3))
  .dematerialize()
  .collect { println("dematerialize: $it") }
```

Output:

```none
dematerialize: 1
dematerialize: 2
dematerialize: 3
```

----

#### raceWith / ambWith

- ReactiveX docs: http://reactivex.io/documentation/operators/amb.html
- Similar
  to [RxJava ambWith](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#ambWith-org.reactivestreams.Publisher-)
  .
- Similar to [RxJS raceWith](https://rxjs.dev/api/operators/raceWith)

Mirrors the current `Flow` or the other `Flow`s provided of which the first either emits a value
or sends a termination event (error or complete event).

```kotlin

flow {
  delay(100)
  emit(1)
  emit(2)
  emit(3)
}.raceWith(
  flow {
    delay(200)
    emit(2)
    emit(3)
    emit(4)
  }
).collect { println("raceWith: $it") }
```

Output:

```none
raceWith: 1
raceWith: 2
raceWith: 3
```

----

#### pairwise / zipWithNext

- Similar to [RxJS pairwise](https://rxjs.dev/api/operators/pairwise)

Groups pairs of consecutive emissions together and emits them as a pair.
Emits the `(n)th` and `(n-1)th` events as a pair.
The first value won't be emitted until the second one arrives.

Note, `zipWithNext` is an alias to `pairwise`.

```kotlin
range(0, 4)
  .pairwise()
  .collect { println("pairwise: $it") }

println("---")

range(0, 4)
  .zipWithNext { a, b -> "$a -> $b" }
  .collect { println("zipWithNext: $it") }
```

Output:

```none
pairwise: (0, 1)
pairwise: (1, 2)
pairwise: (2, 3)
---
zipWithNext: 0 -> 1
zipWithNext: 1 -> 2
zipWithNext: 2 -> 3
```

----

#### repeat

- Similar to [RxJS repeat](https://rxjs.dev/api/index/function/repeat)

Returns a `Flow` that will recollect to the source stream when the source stream completes.

```kotlin
flowFromSuspend {
  println("Start collecting...")

  Random
    .nextInt(0..3)
    .also { println("Emit: $it") }
}
  .repeat(
    delay = 1.seconds,
    count = 10
  )
  .filter { it == 2 }
  .take(1)
  .collect { println("repeat: $it") }
```

Output:

```none
Start collecting...
Emit: 1
Start collecting...
Emit: 3
Start collecting...
Emit: 1
Start collecting...
Emit: 0
Start collecting...
Emit: 1
Start collecting...
Emit: 3
Start collecting...
Emit: 2
repeat: 2
```

----

#### retryWhenWithDelayStrategy

Retries collection of the given flow when an exception occurs in the upstream flow and the
`predicate` returns true. The predicate also receives an `attempt` number as parameter,
starting from zero on the initial call. When `predicate` returns true, the next retries will be
delayed after a duration computed by `DelayStrategy.nextDelay`.

- ReactiveX docs: https://reactivex.io/documentation/operators/retry.html

```kotlin
var count = -1

flowFromSuspend {
  ++count
  println("Call count=$count")

  when (count) {
    0 -> throw MyException(message = "Will retry...", cause = null)
    1 -> "Result: count=$count"
    else -> error("Unexpected: count=$count")
  }
}
  .retryWhenWithDelayStrategy(
    strategy = DelayStrategy.FixedTimeDelayStrategy(duration = 200.milliseconds),
    predicate = { cause, attempt -> cause is MyException && attempt < 1 }
  )
  .collect { println("retryWhenWithDelayStrategy: $it") }
```

Output:

```none
Call count=0
Call count=1
retryWhenWithDelayStrategy: Result: count=1
```

----

#### retryWhenWithExponentialBackoff

- ReactiveX docs: https://reactivex.io/documentation/operators/retry.html

Retries collection of the given flow with exponential backoff delay strategy
when an exception occurs in the upstream flow and the `predicate` returns true. When `predicate` returns true,
the next retries will be delayed after a duration computed by `DelayStrategy.ExponentialBackoffDelayStrategy`.

```kotlin
var count = -1

flowFromSuspend {
  ++count
  println("Call count=$count")

  when (count) {
    0 -> throw MyException(message = "Will retry...", cause = null)
    1 -> "Result: count=$count"
    else -> error("Unexpected: count=$count")
  }
}
  .retryWhenWithExponentialBackoff(
    initialDelay = 500.milliseconds,
    factor = 2.0,
  ) { cause, attempt -> cause is MyException && attempt < 1 }
  .collect { println("retryWhenWithExponentialBackoff: $it") }
```

Output:

```none
Call count=0
Call count=1
retryWhenWithExponentialBackoff: Result: count=1
```

----

#### retryWithExponentialBackoff

- ReactiveX docs: https://reactivex.io/documentation/operators/retry.html

Retries collection of the given flow with exponential backoff delay strategy
when an exception occurs in the upstream flow and the `predicate` returns true. When `predicate` returns true,
the next retries will be delayed after a duration computed by `DelayStrategy.ExponentialBackoffDelayStrategy`.

```kotlin
var count = -1

flowFromSuspend {
  ++count
  println("Call count=$count")

  when (count) {
    0 -> throw MyException(message = "Will retry...", cause = null)
    1 -> "Result: count=$count"
    else -> error("Unexpected: count=$count")
  }
}
  .retryWithExponentialBackoff(
    maxAttempt = 2,
    initialDelay = 500.milliseconds,
    factor = 2.0,
  ) { it is MyException }
  .collect { println("retryWithExponentialBackoff: $it") }
```

Output:

```none
Call count=0
Call count=1
retryWithExponentialBackoff: Result: count=1
```

----

#### scanWith

- Similar
  to [RxJava scanWith](https://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#scanWith-io.reactivex.rxjava3.functions.Supplier-io.reactivex.rxjava3.functions.BiFunction-)

Folds the given flow with [operation], emitting every intermediate result,
including the initial value supplied by [initialSupplier] at the collection time.

This is a variant of `scan` that the initial value is lazily supplied,
which is useful when the initial value is expensive to create
or depends on a logic that should be executed at the collection time (lazy semantics).

```kotlin
var count = 0
val mutex = Mutex()

suspend fun calculateInitialValue(): Int {
  println("calculateInitialValue")
  delay(1000)
  return mutex.withLock { count++ }
}

flowOf(1, 2, 3)
  .scanWith(::calculateInitialValue) { acc, e -> acc + e }
  .collect { println("scanWith[1]: $it") }

flowOf(1, 2, 3)
  .scanWith(::calculateInitialValue) { acc, e -> acc + e }
  .collect { println("scanWith[2]: $it") }
```

Output:

```none
calculateInitialValue
scanWith[1]: 0
scanWith[1]: 1
scanWith[1]: 3
scanWith[1]: 6
calculateInitialValue
scanWith[2]: 1
scanWith[2]: 2
scanWith[2]: 4
scanWith[2]: 7
```

----

#### select

- Inspirited by [NgRx memoized selector](https://ngrx.io/guide/store/selectors).
- Selectors are pure functions used for obtaining slices of a Flow of state.
  `FlowExt` provides a few helper functions for optimizing this selection.

  - Selectors can compute derived data, to store the minimal possible state.
  - Selectors are efficient. A selector is not recomputed unless one of its arguments changes.
  - When using the [select] functions, it will keep track of the latest arguments in which your selector function was invoked.
    Because selectors are pure functions, the last result can be returned
    when the arguments match without re-invoking your selector function.
    This can provide performance benefits, particularly with selectors that perform expensive computation.
    This practice is known as memoization.

```kotlin
data class UiState(
  val items: List<String> = emptyList(),
  val term: String? = null,
  val isLoading: Boolean = false,
  val error: Throwable? = null
)

flow {
  println("select: emit 1")
  emit(UiState())

  println("select: emit 2")
  emit(
    UiState(
      items = listOf("a", "b", "c"),
      term = "a",
      isLoading = true,
      error = Throwable("error")
    )
  )

  println("select: emit 3")
  emit(
    UiState(
      items = listOf("a", "b", "c"),
      term = "a",
      isLoading = false,
      error = Throwable("error")
    )
  )

  println("select: emit 4")
  emit(
    UiState(
      items = listOf("a", "b", "c"),
      term = "b",
      isLoading = false,
      error = Throwable("error")
    )
  )
}
  .select(
    selector1 = { it.items },
    selector2 = { it.term },
    projector = { items, term ->
      term?.let { v ->
        items.filter { it.contains(v, ignoreCase = true) }
      }
    }
  )
  .collect { println("select: $it") }
```

Output:

```none
select: emit 1
select: null
select: emit 2
select: [a]
select: emit 3
select: emit 4
select: [b]
```

----

#### skipUntil / dropUntil

- ReactiveX docs: https://reactivex.io/documentation/operators/skipuntil.html
- Similar to [RxJS skipUntil](https://rxjs.dev/api/index/function/skipUntil)
- Similar
  to [RxJava skipUntil](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#skipUntil-org.reactivestreams.Publisher-)

Returns a `Flow` that skips items emitted by the source `Flow` until a second `Flow` emits a value or completes.

```kotlin
flowOf(1, 2, 3)
  .onEach { delay(100) }
  .skipUntil(timer(Unit, 150))
  .collect { println("skipUntil: $it") }
```

Output:

```none
skipUntil: 2
skipUntil: 3
```

----

#### takeUntil

- ReactiveX docs: http://reactivex.io/documentation/operators/takeuntil.html
- Similar to [RxJS takeUntil](https://rxjs.dev/api/operators/takeUntil)

Emits the values emitted by the source `Flow` until a `notifier Flow` emits a value or completes.

```kotlin
range(0, 5)
  .onEach { delay(100) }
  .takeUntil(timer(Unit, 270.milliseconds))
  .collect { println("takeUntil: $it") }
```

Output:

```none
takeUntil: 0
takeUntil: 1
```

----

#### throttleTime

- ReactiveX docs: https://reactivex.io/documentation/operators/debounce.html
- Similar to [RxJS throttleTime](https://rxjs.dev/api/operators/throttleTime)

Returns a `Flow` that emits a value from the source `Flow`, then ignores subsequent source values
for a duration determined by `durationSelector`, then repeats this process for the next source value.

```kotlin
(1..10)
  .asFlow()
  .onEach { delay(200) }
  .throttleTime(500)
  .collect { println("throttleTime: $it") }
```

Output:

```none
throttleTime: 1
throttleTime: 4
throttleTime: 7
throttleTime: 10
```

----

#### withLatestFrom

- RxMarbles: https://rxmarbles.com/#withLatestFrom
- Similar to [RxJS withLatestFrom](https://rxjs.dev/api/operators/withLatestFrom)

Merges two `Flow`s into one `Flow` by combining each value from self with the latest value from the second `Flow`, if any.
Values emitted by self before the second `Flow` has emitted any values will be omitted.

```kotlin
range(0, 5)
  .onEach { delay(100) }
  .withLatestFrom(
    range(0, 10)
      .onEach { delay(70) }
  )
  .collect { println("withLatestFrom: $it") }
```

Output:

```none
withLatestFrom: (0, 0)
withLatestFrom: (1, 1)
withLatestFrom: (2, 3)
withLatestFrom: (3, 4)
withLatestFrom: (4, 6)
```

... and more, please check out [Docs 1.x](https://hoc081098.github.io/FlowExt/docs/1.x)/[Docs
snapshot](https://hoc081098.github.io/FlowExt/docs/latest).

## License

```License
MIT License

Copyright (c) 2021-2024 Petrus Nguyễn Thái Học
```

[badge-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat
[badge-android-native]: http://img.shields.io/badge/support-[AndroidNative]-6EDB8D.svg?style=flat
[badge-wearos]: http://img.shields.io/badge/-wearos-8ECDA0.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat
[badge-js]: http://img.shields.io/badge/-js-F8DB5D.svg?style=flat
[badge-js-ir]: https://img.shields.io/badge/support-[IR]-AAC4E0.svg?style=flat
[badge-nodejs]: https://img.shields.io/badge/-nodejs-68a063.svg?style=flat
[badge-linux]: http://img.shields.io/badge/-linux-2D3F6C.svg?style=flat
[badge-windows]: http://img.shields.io/badge/-windows-4D76CD.svg?style=flat
[badge-wasm]: https://img.shields.io/badge/-wasm-624FE8.svg?style=flat
[badge-apple-silicon]: http://img.shields.io/badge/support-[AppleSilicon]-43BBFF.svg?style=flat
[badge-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg?style=flat
[badge-mac]: http://img.shields.io/badge/-macos-111111.svg?style=flat
[badge-watchos]: http://img.shields.io/badge/-watchos-C0C0C0.svg?style=flat
[badge-tvos]: http://img.shields.io/badge/-tvos-808080.svg?style=flat
