# FlowExt

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hoc081098/FlowExt?style=flat)](https://search.maven.org/search?q=io.github.hoc081098)
[![codecov](https://codecov.io/gh/hoc081098/FlowExt/branch/master/graph/badge.svg?token=9KGcZ7P2hV)](https://codecov.io/gh/hoc081098/FlowExt)
[![Build](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml/badge.svg)](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml)
[![Validate Gradle Wrapper](https://github.com/hoc081098/FlowExt/actions/workflows/gradle-wrapper-validation.yml/badge.svg)](https://github.com/hoc081098/FlowExt/actions/workflows/gradle-wrapper-validation.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin version](https://img.shields.io/badge/Kotlin-1.6.21-blueviolet?logo=kotlin&logoColor=white)](http://kotlinlang.org)
[![KotlinX Coroutines version](https://img.shields.io/badge/Kotlinx_Coroutines-1.6.1-blueviolet?logo=kotlin&logoColor=white)](https://github.com/Kotlin/kotlinx.coroutines/releases/tag/1.6.1)
![badge][badge-jvm]
![badge][badge-android]
![badge][badge-ios]
![badge][badge-watchos]
![badge][badge-tvos]
![badge][badge-mac]
![badge][badge-linux]
![badge][badge-js]
![badge][badge-windows]
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fhoc081098%2FFlowExt&count_bg=%2379C83D&title_bg=%23555555&icon=kotlin.svg&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)

- FlowExt is a Kotlin Multiplatform library, that provides many operators and extensions to Kotlin Coroutines Flow.
- FlowExt provides a collection of operators, Flows and utilities for Flow, that are not provided by Kotlinx Coroutine
  themselves, but are common in other Reactive Frameworks (rxjs, RxJava, RxSwift, rxdart, ...) and standards.

> Kotlinx Coroutines Flow Extensions. Extensions to the Kotlin Flow library. Kotlin Flow extensions.
> Multiplatform Kotlinx Coroutines Flow Extensions. Multiplatform Extensions to the Kotlin Flow
> library. Multiplatform Kotlin Flow extensions. RxJS Kotlin Coroutines Flow. RxSwift Kotlin
> Coroutines Flow. RxJava Kotlin Coroutines Flow. RxJS Kotlin Flow. RxSwift Kotlin Flow. RxJava Kotlin
> Flow. RxJS Coroutines Flow. RxSwift Coroutines Flow. RxJava Coroutines Flow.

## Author: [Petrus Nguyễn Thái Học](https://github.com/hoc081098)

## API

### 0.x release docs: https://hoc081098.github.io/FlowExt/docs/0.x

### Snapshot docs: https://hoc081098.github.io/FlowExt/docs/latest

### Table of contents

- Create
  - [`concat`](#concat)
  - [`defer`](#defer)
  - [`flowFromSuspend`](#flowFromSuspend)
  - [`interval`](#interval)
  - [`neverFlow`](#neverFlow)
  - [`race`](#race--amb)
  - [`amb`](#race--amb)
  - [`range`](#range)
  - [`timer`](#timer)
- Intermediate operators
  - [`bufferCount`](#bufferCount)
  - [`concatWith`](#concatWith)
  - [`startWith`](#startWith)
  - [`flatMapFirst`](#flatmapfirst--exhaustmap)
  - [`exhaustMap`](#flatmapfirst--exhaustmap)
  - [`flattenFirst`](#flattenfirst--exhaustall)
  - [`flatMapConcatEager`](#flatmapconcateager)
  - `mapEager`
  - `flattenEager`
  - [`exhaustAll`](#flattenfirst--exhaustall)
  - `mapIndexed`
  - [`mapTo`](#mapTo)
  - `mapToUnit`
  - [`materialize`](#materialize)
  - [`dematerialize`](#dematerialize)
  - [`raceWith`](#racewith--ambwith)
  - [`ambWith`](#racewith--ambwith)
  - [`pairwise`](#pairwise)
  - `retryWhenWithDelayStrategy`
  - `retryWhenWithExponentialBackoff`
  - `retryWithExponentialBackoff`
  - [`skipUntil`](#skipuntil--dropuntil)
  - [`dropUntil`](#skipuntil--dropuntil)
  - [`takeUntil`](#takeUntil)
  - `throttleTime`
  - [`withLatestFrom`](#withLatestFrom)

#### bufferCount

- Similar to [RxJS bufferCount](https://rxjs.dev/api/operators/bufferCount)
- Similar
  to [RxJava buffer](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#buffer-int-int-)

#### concat

- Similar to [RxJS concat](https://rxjs.dev/api/index/function/concat)
- Similar
  to [RxJava concat](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#concat-java.lang.Iterable-)

#### defer

- Similar to [RxJS defer](https://rxjs.dev/api/index/function/defer)
- Similar
  to [RxJava defer](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#defer-io.reactivex.rxjava3.functions.Supplier-)

#### flowFromSuspend

- Similar to [RxJava fromCallable](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#fromCallable-java.util.concurrent.Callable-)

#### interval

- Similar to [RxJS interval](https://rxjs.dev/api/index/function/interval)

#### neverFlow

- Similar to [RxJS NEVER](https://rxjs.dev/api/index/const/NEVER)

#### race / amb

- ReactiveX docs: http://reactivex.io/documentation/operators/amb.html
- Similar
  to [RxJava amb](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#amb-java.lang.Iterable-)
  .
- Similar to [RxJS race](https://rxjs.dev/api/index/function/race)

#### range

- ReactiveX docs: http://reactivex.io/documentation/operators/range.html
- Similar to [RxJS range](https://rxjs.dev/api/index/function/range)

#### timer

- ReactiveX docs: http://reactivex.io/documentation/operators/timer.html
- Similar to [RxJS timer](https://rxjs.dev/api/index/function/timer)

#### concatWith

- Similar to [RxJS concatWith](https://rxjs.dev/api/operators/concatWith)
- Similar
  to [RxJava concatWith](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#concatWith-org.reactivestreams.Publisher-)

#### startWith

- Similar to [RxJS startWith](https://rxjs.dev/api/operators/startWith)
- Similar
  to [RxJava startWith](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#startWithItem-T-)

#### flatMapFirst / exhaustMap

- Similar to [RxJS exhaustMap](https://rxjs.dev/api/operators/exhaustMap)
- Similar
  to [RxSwift flatMapFirst](https://github.com/ReactiveX/RxSwift/blob/b48f2e9536cd985d912126709b97bd743e58c8fc/RxSwift/Observables/Merge.swift#L37)

#### flattenFirst / exhaustAll

- Similar to [RxJS exhaustAll](https://rxjs.dev/api/operators/exhaustAll)

#### flatMapConcatEager

- Similar to [RxJava concatMapEager](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#concatMapEager-io.reactivex.rxjava3.functions.Function-)

#### mapTo

- Similar to [RxJS mapTo](https://rxjs.dev/api/operators/mapTo)

#### materialize

- Similar to [RxJS materialize](https://rxjs.dev/api/operators/materialize)
- Similar
  to [RxJava materialize](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#materialize--)

#### dematerialize

- Similar to [RxJS dematerialize](https://rxjs.dev/api/operators/dematerialize)
- Similar
  to [RxJava dematerialize](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#dematerialize--)

#### raceWith / ambWith

- ReactiveX docs: http://reactivex.io/documentation/operators/amb.html
- Similar
  to [RxJava ambWith](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#ambWith-org.reactivestreams.Publisher-)
  .
- Similar to [RxJS raceWith](https://rxjs.dev/api/operators/raceWith)

#### pairwise

- Similar to [RxJS pairwise](https://rxjs.dev/api/operators/pairwise)

#### skipUntil / dropUntil

- ReactiveX docs: https://reactivex.io/documentation/operators/skipuntil.html
- Similar to [RxJS skipUntil](https://rxjs.dev/api/index/function/skipUntil)
- Similar to [RxJava skipUntil](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#skipUntil-org.reactivestreams.Publisher-)

#### takeUntil

- ReactiveX docs: http://reactivex.io/documentation/operators/takeuntil.html
- Similar to [RxJS takeUntil](https://rxjs.dev/api/operators/takeUntil)

#### withLatestFrom

- RxMarbles: https://rxmarbles.com/#withLatestFrom
- Similar to [RxJS withLatestFrom](https://rxjs.dev/api/operators/withLatestFrom)

... and more, please check out [Docs 0.x](https://hoc081098.github.io/FlowExt/docs/0.x)/[Docs
snapshot](https://hoc081098.github.io/FlowExt/docs/latest).

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
implementation("io.github.hoc081098:FlowExt:0.3.0")
```

### JVM / Android only

```groovy
implementation("io.github.hoc081098:FlowExt-jvm:0.3.0")
```

### Native binaries

```groovy
implementation("io.github.hoc081098:FlowExt-iosx64:0.3.0")
implementation("io.github.hoc081098:FlowExt-iosarm64:0.3.0")
implementation("io.github.hoc081098:FlowExt-iosarm32:0.3.0")
implementation("io.github.hoc081098:FlowExt-watchosx86:0.3.0")
implementation("io.github.hoc081098:FlowExt-watchosx64:0.3.0")
implementation("io.github.hoc081098:FlowExt-watchosarm64:0.3.0")
implementation("io.github.hoc081098:FlowExt-watchosarm32:0.3.0")
implementation("io.github.hoc081098:FlowExt-tvosx64:0.3.0")
implementation("io.github.hoc081098:FlowExt-tvosxarm64:0.3.0")
implementation("io.github.hoc081098:FlowExt-macosx64:0.3.0")
implementation("io.github.hoc081098:FlowExt-mingwx64:0.3.0")
implementation("io.github.hoc081098:FlowExt-linuxx64:0.3.0")
```

### Snapshot

Snapshots of the development version are available in Sonatype's snapshots repository.

- Kotlin

```kotlin
allprojects {
  repositories {
    ...
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}

dependencies {
  implementation("io.github.hoc081098:FlowExt:0.4.0-SNAPSHOT")
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
  implementation("io.github.hoc081098:FlowExt:0.4.0-SNAPSHOT")
}
```

## License

```License
MIT License

Copyright (c) 2021-2022 Petrus Nguyễn Thái Học
```

[badge-android]: http://img.shields.io/badge/android-6EDB8D.svg?style=flat

[badge-ios]: http://img.shields.io/badge/ios-CDCDCD.svg?style=flat

[badge-js]: http://img.shields.io/badge/js-F8DB5D.svg?style=flat

[badge-jvm]: http://img.shields.io/badge/jvm-DB413D.svg?style=flat

[badge-linux]: http://img.shields.io/badge/linux-2D3F6C.svg?style=flat

[badge-windows]: http://img.shields.io/badge/windows-4D76CD.svg?style=flat

[badge-mac]: http://img.shields.io/badge/macos-111111.svg?style=flat

[badge-watchos]: http://img.shields.io/badge/watchos-C0C0C0.svg?style=flat

[badge-tvos]: http://img.shields.io/badge/tvos-808080.svg?style=flat

[badge-wasm]: https://img.shields.io/badge/wasm-624FE8.svg?style=flat

[badge-nodejs]: https://img.shields.io/badge/nodejs-68a063.svg?style=flat
