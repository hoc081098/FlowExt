# FlowExt

[![CI](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml/badge.svg)](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-1.5.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
![badge][badge-jvm]
![badge][badge-android]
![badge][badge-ios]
![badge][badge-watchos]
![badge][badge-tvos]
![badge][badge-mac]
![badge][badge-linux]
![badge][badge-js]
![badge][badge-windows]

Kotlinx Coroutines Flow Extensions.
Extensions to the Kotlin Flow library.

# API

-   [flatMapFirst]()/[exhaustMap]() (similar to [RxJS exhaustMap](https://rxjs.dev/api/operators/exhaustMap), [RxSwift flatMapFirst](https://github.com/ReactiveX/RxSwift/blob/1a1fa37b0d08e0f99ffa41f98f340e8bc60c35c4/RxSwift/Observables/Merge.swift#L37))\
    Projects each source value to a Flow which is merged in the output Flow only if the previous projected Flow has completed.
    If value is received while there is some projected Flow sequence being merged it will simply be ignored.

-   [flattenFirst]()/[exhaustAll]() (similar to [RxJs exhaustAll](https://rxjs.dev/api/operators/exhaustAll))\
    Converts a higher-order Flow into a first-order Flow by dropping inner Flow while the previous inner Flow has not yet completed.

-   [range]() (similar to [RxJS range](https://rxjs.dev/api/index/function/range))\
    Creates a Flow that emits a sequence of numbers within a specified range.

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