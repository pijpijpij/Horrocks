package com.pij.horrocks.kotlin

import io.reactivex.Observable
import io.reactivex.Observer

/** When applied to a state (S), it transforms it. This is expected to use immutability, i.e. it
 *  must not modify the state it receives.
 */
typealias Reducer<S> = (S) -> S

/** Essential abstraction: it responds to external events by emitting a stream of [Reducer]s.
 */
interface Feature<E : Any, S : Any> {

    fun externalEvent(event: E)

    /** Receives changes to the state. The feature can use this as the context to creating
     * its [Reducer]s. It can also use this as an internal event that will cause [Reducer]s to be
     * emitted.
     */
    val context: Observer<S>

    /** Stream of [Reducer] that this feature creates in reaction to calls to [externalEvent]. All
     * reducers in that stream must support immutability, i.e. they must not modify the state they
     * receive.
     */
    val reducers: Observable<Reducer<S>>

}