package com.pij.horrocks.kotlin

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLogger

class Engine<S : Any>(
        initialState: S,
        private val clearEventProperties: S.() -> S = { this },
        foreground: Scheduler? = null,
        log4J: Logger? = Companion.logger,
        private val features: Collection<Feature<*, S>>
) {

    companion object {
        private val logger: Logger by lazy { LoggerFactory.getLogger(Engine::class.java) }

        fun <S : Any> create(
                initialState: S,
                clearEventProperties: S.() -> S,
                foreground: Scheduler,
                logger: Logger,
                vararg features: Feature<*, S>
        ) = Engine(
                initialState = initialState,
                clearEventProperties = clearEventProperties,
                foreground = foreground,
                log4J = logger,
                features = features.toList()
        )

        fun <S : Any> create(
                initialState: S,
                clearEventProperties: S.() -> S,
                logger: Logger,
                vararg features: Feature<*, S>
        ) = Engine(
                initialState = initialState,
                clearEventProperties = clearEventProperties,
                log4J = logger,
                features = features.toList()
        )

        fun <S : Any> create(
                initialState: S,
                clearEventProperties: S.() -> S,
                vararg features: Feature<*, S>
        ) = Engine(
                initialState = initialState,
                clearEventProperties = clearEventProperties,
                features = features.toList()
        )

        fun <S : Any> create(
                initialState: S,
                clearEventProperties: S.() -> S,
                foreground: Scheduler,
                vararg features: Feature<*, S>
        ) = Engine(
                initialState = initialState,
                clearEventProperties = clearEventProperties,
                foreground = foreground,
                features = features.toList()
        )

        fun <S : Any> create(
                initialState: S,
                clearEventProperties: S.() -> S,
                logger: Logger,
                foreground: Scheduler,
                vararg features: Feature<*, S>
        ) = Engine(
                initialState = initialState,
                clearEventProperties = clearEventProperties,
                log4J = logger,
                foreground = foreground,
                features = features.toList()
        )

        internal fun <T> Observable<T>.subscribeSafely(display: (T) -> Unit, logger: Logger = NOPLogger.NOP_LOGGER, stallingAction: (Throwable) -> Unit = {}): Disposable = this
                .subscribe({
                    try {
                        display(it)
                    } catch (e: Throwable) {
                        logger.warn("${Engine::class.java} Failure while displaying '$it'", e)
                    }
                }, {
                    logger.error("${Engine::class.java} Engine failure", it)
                    try {
                        stallingAction(it)
                    } catch (ignored: Throwable) {
                        logger.error("${Engine::class.java} Could not perform a last ditch action before stalling.", ignored)
                    }
                })
    }

    private val logger: Logger = log4J ?: NOPLogger.NOP_LOGGER

    private fun reducers() = features.map {
        it.reducers.doOnError { error -> logger.warn("$javaClass Failed to emit a reducer", error) }
    }

    fun subscribe(display: (S) -> Unit, stallingAction: (Throwable) -> Unit = {}): Disposable = states.subscribeSafely(display, logger, stallingAction)

    internal val states: Observable<S>

    init {
        require(features.isNotEmpty()) { "Must provide a least one feature to spin" }

        states = Observable.merge(reducers())
                .scan(initialState) { state, reducer: Reducer<S> -> reducer.reduceSafely(state) }
                .doOnNext { logger.debug("$javaClass $it") }
                .doAfterNext { state -> features.forEach { it.context.onNext(state) } }
                .run { if (foreground == null) this else observeOn(foreground) }
                .replay(1)
                .refCount()
    }

    private fun Reducer<S>.reduceSafely(state: S) = try {
        invoke(state.clearEventProperties())
    } catch (e: Throwable) {
        logger.warn("$javaClass Failed reducer", e)
        throw e
    }

}