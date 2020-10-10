package com.pij.horrocks.kotlin

import io.reactivex.Observable
import io.reactivex.Single

/**
 * Main implementation of the DSL defined in [feature].
 */
internal class FeatureBuilder<E : Any, S : Any> : FeatureConfiguration<E, S> {

    private lateinit var _triggeredStream: (event: E, currentState: S) -> Observable<Reducer<S>>
    override fun triggeredStream(newValue: (event: E, currentState: S) -> Observable<Reducer<S>>) {
        require(!this::_triggeredStream.isInitialized) { "Cannot define triggeredStream twice" }
        _triggeredStream = newValue
    }

    override fun triggeredStream(newValue: Observable<Reducer<S>>) = triggeredStream { _, _ -> newValue }

    override fun triggeredSingle(newValue: (event: E, currentState: S) -> Single<Reducer<S>>) {
        require(!this::_triggeredStream.isInitialized) { "Cannot define triggeredSingle twice" }
        triggeredStream { event: E, currentState: S -> newValue(event, currentState).toObservable() }
    }

    override fun triggeredSingle(newValue: Single<Reducer<S>>) = triggeredSingle { _, _ -> newValue }

    private lateinit var _onError: (e: Throwable) -> Reducer<S>
    override fun onError(newValue: (e: Throwable) -> Reducer<S>) {
        require(!this::_onError.isInitialized) { "Cannot define onError twice" }
        _onError = newValue
    }

    private lateinit var _startWith: () -> E
    override fun startWith(first: () -> E) {
        require(!this::_startWith.isInitialized) { "Cannot define startWith twice" }
        _startWith = first
    }

    private lateinit var _stateEvents: (Observable<S>) -> Observable<out E>
    override fun stateEvents(stateAsEvent: (Observable<S>) -> Observable<out E>) {
        require(!this::_stateEvents.isInitialized) { "Cannot define stateEvents twice" }
        _stateEvents = stateAsEvent
    }

    fun build(): Feature<E, S> = PojoFeature(
            _triggeredStream,
            _onError,
            if (this::_stateEvents.isInitialized) _stateEvents else { _ -> Observable.never<E>() },
            if (this::_startWith.isInitialized) _startWith() else null
    )
}