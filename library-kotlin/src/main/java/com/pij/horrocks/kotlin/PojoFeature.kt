package com.pij.horrocks.kotlin

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/** Internal implementation of a [Feature].
 */
internal class PojoFeature<E : Any, S : Any>(
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

    override val reducers: Observable<Reducer<S>> = allEvents
            .withLatestFrom(other = states) { event, state -> onTrigger(event, state).onErrorReturn { onError(it) } }
            .flatMap { it }

}