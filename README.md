# FlowExt

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hoc081098/FlowExt?style=flat)](https://search.maven.org/search?q=io.github.hoc081098)
[![CI](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml/badge.svg)](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin version](https://img.shields.io/badge/kotlin-1.5.20-blueviolet?logo=kotlin&logoColor=white)](http://kotlinlang.org)
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

# API

-   **bufferCount** (similar to [RxJS bufferCount](https://rxjs.dev/api/operators/bufferCount), [RxJava buffer](http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#buffer-int-int-))\
    > Buffers the source [Flow] values until the size hits the maximum [bufferSize] given.

-   **`flatMapFirst`**/**`exhaustMap`** (similar to [RxJS exhaustMap](https://rxjs.dev/api/operators/exhaustMap), [RxSwift flatMapFirst](https://github.com/ReactiveX/RxSwift/blob/b48f2e9536cd985d912126709b97bd743e58c8fc/RxSwift/Observables/Merge.swift#L37))\
    > Projects each source value to a Flow which is merged in the output Flow only if the previous projected Flow has completed.
    > If value is received while there is some projected Flow sequence being merged it will simply be ignored.

-   **`flattenFirst`**/**`exhaustAll`** (similar to [RxJS exhaustAll](https://rxjs.dev/api/operators/exhaustAll))\
    > Converts a higher-order Flow into a first-order Flow by dropping inner Flow while the previous inner Flow has not yet completed.

-   **`mapTo`** (similar to [RxJS mapTo](https://rxjs.dev/api/operators/mapTo))\
    > Emits the given constant value on the output Flow every time the source Flow emits a value.

-   **`range`** (http://reactivex.io/documentation/operators/range.html) (similar to [RxJS range](https://rxjs.dev/api/index/function/range))\
    > Creates a Flow that emits a sequence of numbers within a specified range.

-   **`takeUntil`** (http://reactivex.io/documentation/operators/takeuntil.html) (similar to [RxJS takeUntil](https://rxjs.dev/api/operators/takeUntil))\
    > Emits the values emitted by the source Flow until a notifier Flow emits a value or completes.

-   **`timer`** (http://reactivex.io/documentation/operators/timer.html) (similar to [RxJS timer](https://rxjs.dev/api/index/function/timer))\
    > Creates a Flow that will wait for a specified time, before emitting the given value.

-   **`withLatestFrom`** (https://rxmarbles.com/#withLatestFrom) (similar to [RxJS withLatestFrom](https://rxjs.dev/api/operators/withLatestFrom))\
    > Merges two Flows into one Flow by combining each value from self with the latest value from the second Flow, if any.
    > Values emitted by self before the second Flow has emitted any values will be omitted.

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
implementation("io.github.hoc081098:FlowExt:0.0.6")
```

### JVM / Android only
```groovy
implementation("io.github.hoc081098:FlowExt-jvm:0.0.6")
```

### Native binaries
```groovy
implementation("io.github.hoc081098:FlowExt-iosx64:0.0.6")
implementation("io.github.hoc081098:FlowExt-iosarm64:0.0.6")
implementation("io.github.hoc081098:FlowExt-iosarm32:0.0.6")
implementation("io.github.hoc081098:FlowExt-watchosx86:0.0.6")
implementation("io.github.hoc081098:FlowExt-watchosx64:0.0.6")
implementation("io.github.hoc081098:FlowExt-watchosarm64:0.0.6")
implementation("io.github.hoc081098:FlowExt-watchosarm32:0.0.6")
implementation("io.github.hoc081098:FlowExt-tvosx64:0.0.6")
implementation("io.github.hoc081098:FlowExt-tvosxarm64:0.0.6")
implementation("io.github.hoc081098:FlowExt-macosx64:0.0.6")
implementation("io.github.hoc081098:FlowExt-mingwx64:0.0.6")
implementation("io.github.hoc081098:FlowExt-linuxx64:0.0.6")
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
  implementation("io.github.hoc081098:FlowExt:0.0.7-SNAPSHOT")
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
  implementation("io.github.hoc081098:FlowExt:0.0.7-SNAPSHOT")
}
```


## License

```License
MIT License

Copyright (c) 2021 Petrus Nguyễn Thái Học

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

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
