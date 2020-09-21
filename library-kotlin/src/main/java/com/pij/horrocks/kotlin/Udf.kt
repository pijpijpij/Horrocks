package com.pij.horrocks.kotlin

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

typealias Reducer<S> = (S) -> S

interface Feature<E : Any, S : Any> {

    fun externalEvent(event: E)

    val context: Observer<S>

    val reducers: Observable<Reducer<S>>

}

private class PojoFeature<E : Any, S : Any>(
        private val onTrigger: (event: E, currentState: S) -> Observable<Reducer<S>>,
        private val onError: (e: Throwable) -> Reducer<S>,
        private val stateEvents: (Observable<S>) -> Observable<out E>,
        startWith: E? = null
) : Feature<E, S> {

    /** We use a Behaviour subject in case [#reducers] is not subscribed to before [#context] received is first states. */
    private val states = BehaviorSubject.create<S>()
    override val context: Observer<S> = states

    private val externalEvents = PublishSubject.create<E>()
    override fun externalEvent(event: E) = externalEvents.onNext(event)

    /** [Observable#defer] avoids invoking [#stateEvents] during construction: we want that to run when [#reducers] is subscribed to. */
    private var internalEvents = Observable.defer { stateEvents.invoke(states.hide()) }
    private val firstEvent = if (startWith == null) Observable.empty() else Observable.just(startWith)
    private val allEvents = Observable.merge(externalEvents.startWith(firstEvent), internalEvents)

    override val reducers: Observable<Reducer<S>> =
            allEvents.withLatestFrom(other = states) { event, state -> onTrigger(event, state).onErrorReturn { onError(it) } }
                    .flatMap { it }

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

private class FeatureBuilder<E : Any, S : Any> : FeatureConfiguration<E, S> {

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

fun <E : Any, S : Any> feature(init: FeatureConfiguration<E, S>.() -> Unit): Feature<E, S> {
    val builder = FeatureBuilder<E, S>()
    builder.init()
    return builder.build()
}

