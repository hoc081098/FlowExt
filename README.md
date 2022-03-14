# FlowExt

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hoc081098/FlowExt?style=flat)](https://search.maven.org/search?q=io.github.hoc081098)
[![Build](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml/badge.svg)](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml)
[![Validate Gradle Wrapper](https://github.com/hoc081098/FlowExt/actions/workflows/gradle-wrapper-validation.yml/badge.svg)](https://github.com/hoc081098/FlowExt/actions/workflows/gradle-wrapper-validation.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin version](https://img.shields.io/badge/kotlin-1.5.31-blueviolet?logo=kotlin&logoColor=white)](http://kotlinlang.org)
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

Kotlinx Coroutines Flow Extensions.
Extensions to the Kotlin Flow library.
Kotlin Flow extensions.

## Author: [Petrus Nguyễn Thái Học](https://github.com/hoc081098)

## API

### Docs: https://hoc081098.github.io/FlowExt/docs/0.x
### Snapshot docs: https://hoc081098.github.io/FlowExt/docs/latest

-   **`bufferCount`** (similar to [RxJS bufferCount](https://rxjs.dev/api/operators/bufferCount), [RxJava buffer](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#buffer-int-int-))
-   **`concat`** (similar to [RxJS concat](https://rxjs.dev/api/index/function/concat), [RxJava concat](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#concat-java.lang.Iterable-))
-   **`concatWith`** (similar to [RxJS concatWith](https://rxjs.dev/api/operators/concatWith), [RxJava concatWith](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#concatWith-org.reactivestreams.Publisher-))
-   **`startWith`** (similar to [RxJS startWith](https://rxjs.dev/api/operators/startWith), [RxJava startWith](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#startWithItem-T-))
-   **`flatMapFirst`**/**`exhaustMap`** (similar to [RxJS exhaustMap](https://rxjs.dev/api/operators/exhaustMap), [RxSwift flatMapFirst](https://github.com/ReactiveX/RxSwift/blob/b48f2e9536cd985d912126709b97bd743e58c8fc/RxSwift/Observables/Merge.swift#L37))
-   **`interval`** (similar to [RxJS interval](https://rxjs.dev/api/index/function/interval))
-   **`flattenFirst`**/**`exhaustAll`** (similar to [RxJS exhaustAll](https://rxjs.dev/api/operators/exhaustAll))
-   **`mapTo`** (similar to [RxJS mapTo](https://rxjs.dev/api/operators/mapTo))
-   **`materialize`** (similar to [RxJS materialize](https://rxjs.dev/api/operators/materialize), [RxJava materialize](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#materialize--))
-   **`dematerialize`** (similar to [RxJS dematerialize](https://rxjs.dev/api/operators/dematerialize), [RxJava dematerialize](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Flowable.html#dematerialize--))
-   **`range`** (http://reactivex.io/documentation/operators/range.html) (similar to [RxJS range](https://rxjs.dev/api/index/function/range))
-   **`takeUntil`** (http://reactivex.io/documentation/operators/takeuntil.html) (similar to [RxJS takeUntil](https://rxjs.dev/api/operators/takeUntil))
-   **`timer`** (http://reactivex.io/documentation/operators/timer.html) (similar to [RxJS timer](https://rxjs.dev/api/index/function/timer))
-   **`withLatestFrom`** (https://rxmarbles.com/#withLatestFrom) (similar to [RxJS withLatestFrom](https://rxjs.dev/api/operators/withLatestFrom))

... and more, please check out [Docs 0.x](https://hoc081098.github.io/FlowExt/docs/0.x)/[Docs snapshot](https://hoc081098.github.io/FlowExt/docs/latest).

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
implementation("io.github.hoc081098:FlowExt:0.2.0")
```

### JVM / Android only
```groovy
implementation("io.github.hoc081098:FlowExt-jvm:0.2.0")
```

### Native binaries
```groovy
implementation("io.github.hoc081098:FlowExt-iosx64:0.2.0")
implementation("io.github.hoc081098:FlowExt-iosarm64:0.2.0")
implementation("io.github.hoc081098:FlowExt-iosarm32:0.2.0")
implementation("io.github.hoc081098:FlowExt-watchosx86:0.2.0")
implementation("io.github.hoc081098:FlowExt-watchosx64:0.2.0")
implementation("io.github.hoc081098:FlowExt-watchosarm64:0.2.0")
implementation("io.github.hoc081098:FlowExt-watchosarm32:0.2.0")
implementation("io.github.hoc081098:FlowExt-tvosx64:0.2.0")
implementation("io.github.hoc081098:FlowExt-tvosxarm64:0.2.0")
implementation("io.github.hoc081098:FlowExt-macosx64:0.2.0")
implementation("io.github.hoc081098:FlowExt-mingwx64:0.2.0")
implementation("io.github.hoc081098:FlowExt-linuxx64:0.2.0")
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
  implementation("io.github.hoc081098:FlowExt:0.3.0-SNAPSHOT")
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
  implementation("io.github.hoc081098:FlowExt:0.3.0-SNAPSHOT")
}
```


## License

```License
MIT License

Copyright (c) 2021 Petrus Nguyễn Thái Học
```

[badge-android]: http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat
[badge-ios]: http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat
[badge-js]: http://img.shields.io/badge/platform-js-F8DB5D.svg?style=flat
[badge-jvm]: http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat
[badge-linux]: http://img.shields.io/badge/platform-linux-2D3F6C.svg?style=flat
[badge-windows]: http://img.shields.io/badge/platform-windows-4D76CD.svg?style=flat
[badge-mac]: http://img.shields.io/badge/platform-macos-111111.svg?style=flat
[badge-watchos]: http://img.shields.io/badge/platform-watchos-C0C0C0.svg?style=flat
[badge-tvos]: http://img.shields.io/badge/platform-tvos-808080.svg?style=flat
[badge-wasm]: https://img.shields.io/badge/platform-wasm-624FE8.svg?style=flat
[badge-nodejs]: https://img.shields.io/badge/platform-nodejs-68a063.svg?style=flat
