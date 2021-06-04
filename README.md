# FlowExt
Kotlinx Coroutine Flow Extensions

[![CI](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml/badge.svg)](https://github.com/hoc081098/FlowExt/actions/workflows/build.yml)

-   [flatMapFirst]()/[exhaustMap]() (similar to [RxJS exhaustMap](https://rxjs.dev/api/operators/exhaustMap), [RxSwift flatMapFirst](https://github.com/ReactiveX/RxSwift/blob/1a1fa37b0d08e0f99ffa41f98f340e8bc60c35c4/RxSwift/Observables/Merge.swift#L37))
    Projects each source value to a Flow which is merged in the output Flow only if the previous projected Flow has completed.
    If value is received while there is some projected Flow sequence being merged it will simply be ignored.

-   [flattenFirst]()/[exhaustAll]() (similar to [RxJs exhaustAll](https://rxjs.dev/api/operators/exhaustAll))
    Converts a higher-order Flow into a first-order Flow by dropping inner Flow while the previous inner Flow has not yet completed.

-   [range]() (similar to [RxJS range](https://rxjs.dev/api/index/function/range))
    Creates a Flow that emits a sequence of numbers within a specified range.
