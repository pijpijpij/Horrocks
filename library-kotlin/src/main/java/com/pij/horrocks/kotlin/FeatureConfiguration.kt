package com.pij.horrocks.kotlin

import io.reactivex.Observable
import io.reactivex.Single

/** Entry-point into the mini-DSL define to simplify creation of [Feature].
 */
fun <E : Any, S : Any> feature(init: FeatureConfiguration<E, S>.() -> Unit): Feature<E, S> {
    val builder = FeatureBuilder<E, S>()
    builder.init()
    return builder.build()
}


interface FeatureConfiguration<E : Any, S : Any> {

    fun triggeredStream(newValue: (event: E, currentState: S) -> Observable<Reducer<S>>)
    fun triggeredStream(newValue: Observable<Reducer<S>>)
    fun triggeredSingle(newValue: (event: E, currentState: S) -> Single<Reducer<S>>)
    fun triggeredSingle(newValue: Single<Reducer<S>>)
    fun onError(newValue: (e: Throwable) -> Reducer<S>)

    /** Optional property.
     * @param first Produces a single event value that will be emitted first when the feature starts ( i.e. when [#reducers] is subscribed to). The value
     * itself is evaluated when the feature is constructed.
     */
    fun startWith(first: () -> E)

    /** Optional method. */
    fun stateEvents(stateAsEvent: (Observable<S>) -> Observable<out E>)

}

