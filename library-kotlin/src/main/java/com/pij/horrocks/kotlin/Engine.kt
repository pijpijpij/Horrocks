package com.pij.horrocks.kotlin

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Engine<S : Any>(
        initialState: S,
        private val clearEventProperties: S.() -> S = { this },
        foreground: Scheduler? = null,
        private val logger: Logger? = Companion.logger,
        private vararg val features: Feature<*, S>
) {

    companion object {
        private val logger: Logger by lazy { LoggerFactory.getLogger(Engine::class.java) }

        fun <S : Any> createWithLogger(
                initialState: S,
                clearEventProperties: S.() -> S,
                foreground: Scheduler,
                vararg features: Feature<*, S>
        ) = Engine(initialState, clearEventProperties, foreground, features = features)

        fun <T> Observable<T>.subscribeSafely(display: (T) -> Unit, logger: Logger? = Companion.logger, stallingAction: (Throwable) -> Unit = {}): Disposable = this
                .subscribe({
                    try {
                        display(it)
                    } catch (e: Throwable) {
                        logger?.warn("Failure while displaying '$it'", e)
                    }
                }, {
                    logger?.error("Engine failure", it)
                    try {
                        stallingAction(it)
                    } catch (ignored: Throwable) {
                        logger?.error("Could not perform a last ditch action before stalling.", ignored)
                    }
                })
    }

    /**Only visible to offer some form of backward compatibility. */
    @Deprecated("use states and subscribeSafely() instead")
    @Suppress("unused")
    val reducers = reducers()
    private fun reducers() = features.map {
        it.reducers.doOnError { error -> logger?.warn("failed to emit a reducer", error) }
    }

    val states: Observable<S>

    init {
        require(features.isNotEmpty()) { "Must provide a least one feature to spin" }

        states = Observable.merge(reducers())
                .scan(initialState) { state, reducer: Reducer<S> -> reducer.reduceSafely(state) }
                .run { if (logger == null) this else doOnNext { logger.debug("$javaClass $it") } }
                .doAfterNext { state -> features.forEach { it.context.onNext(state) } }
                .run { if (foreground == null) this else observeOn(foreground) }
    }

    private fun Reducer<S>.reduceSafely(state: S) = try {
        invoke(state.clearEventProperties())
    } catch (e: Throwable) {
        logger?.warn("$javaClass Failed reducer", e)
        throw e
    }

}