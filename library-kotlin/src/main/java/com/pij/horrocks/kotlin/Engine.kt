package com.pij.horrocks.kotlin

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Engine<S : Any>(
        initialState: S,
        private val clearEventProperties: S.() -> S,
        foreground: Scheduler,
        private val logger: Logger,
        private vararg val features: Feature<*, S>
) {
    constructor(
            initialState: S,
            clearEventProperties: S.() -> S,
            foreground: Scheduler,
            vararg features: Feature<*, S>
    ) : this(initialState, clearEventProperties, foreground, Companion.logger, *features)

    companion object {
        private val logger = LoggerFactory.getLogger(Engine::class.java)

        fun <T> Observable<T>.subscribeSafely(display: (T) -> Unit, stallingAction: (Throwable) -> Unit = {}): Disposable = this
                .subscribe({
                    try {
                        display(it)
                    } catch (e: Throwable) {
                        logger.warn("Failure while displaying '$it'", e)
                    }
                }, {
                    logger.error("Engine failure", it)
                    try {
                        stallingAction(it)
                    } catch (ignored: Throwable) {
                        logger.error("Could not perform a last ditch action before stalling.", ignored)
                    }
                })
    }

    init {
        require(features.isNotEmpty()) { "Must provide a least one feature to spin" }
    }

    /**Only visible to offer some form of backward compatibility. */
    @Deprecated("use states and subscribeSafely() instead")
    @Suppress("unused")
    val reducers = reducers()
    private fun reducers() = features.map {
        it.reducers.doOnError { error -> logger.warn("failed to emit a reducer", error) }
    }

    val states: Observable<S> = Observable.merge(reducers())
            .scan(initialState) { state, reducer: Reducer<S> -> reducer.reduceSafely(state) }
            .doOnNext { logger.debug("${this.javaClass} $it") }
            .doAfterNext { state -> features.forEach { it.context.onNext(state) } }
            .observeOn(foreground)

    private fun Reducer<S>.reduceSafely(state: S) = try {
        invoke(state.clearEventProperties())
    } catch (e: Throwable) {
        logger.warn("Failed reducer", e)
        throw e
    }


}