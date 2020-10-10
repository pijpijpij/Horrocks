package com.pij.horrocks.kotlin

import io.reactivex.Observable
import io.reactivex.Single
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class FeatureConfigurationKtTest {

    @Nested
    inner class FailedSyntax {

        @Test
        fun `'onError' must be provided`() {
            //given
            // when
            val exception = assertThrows<Throwable> {
                feature<Any, String> {
                    triggeredStream { _, _ -> Observable.never() }
                }
            }

            // then
            assertThat(exception.message, containsString("onError"))
        }

        @Test
        fun `'onError' must not be provided twice`() {
            //given
            // when
            val exception = assertThrows<Throwable> {
                feature<Any, String> {
                    triggeredStream { _, _ -> Observable.never() }
                    onError { _ -> { it } }
                    onError { _ -> { it + "2" } }
                }
            }

            // then
            assertThat(exception.message, containsString("onError"))
        }

        @Test
        fun `'triggeredStream' must be provided`() {
            //given
            // when
            val exception = assertThrows<Throwable> {
                feature<Any, String> {
                    onError { _ -> { it } }
                }
            }

            // then
            assertThat(exception.message, containsString("triggeredStream"))
        }

        @Test
        fun `'triggeredStream' must not be provided twice`() {
            //given
            // when
            val exception = assertThrows<Throwable> {
                feature<Any, String> {
                    triggeredStream { _, _ -> Observable.never() }
                    triggeredStream { _, _ -> Observable.never() }
                    onError { _ -> { it } }
                }
            }

            // then
            assertThat(exception.message, containsString("triggeredStream"))
        }

        @Test
        fun `'triggeredSingle' must not be provided with 'triggeredStream'`() {
            //given
            // when
            val exception = assertThrows<Throwable> {
                feature<Any, String> {
                    triggeredSingle { _, _ -> Single.never() }
                    triggeredStream { _, _ -> Observable.never() }
                    onError { _ -> { it } }
                }
            }

            // then
            assertThat(exception.message, anyOf(containsString("triggeredStream"), containsString("triggeredSingle")))
        }

        @Test
        fun `'triggeredSingle' must not be provided twice`() {
            //given
            // when
            val exception = assertThrows<Throwable> {
                feature<Any, String> {
                    triggeredSingle { _, _ -> Single.never() }
                    triggeredSingle { _, _ -> Single.never() }
                    onError { _ -> { it } }
                }
            }

            // then
            assertThat(exception.message, containsString("triggeredSingle"))
        }

        @Test
        fun `'startWith' must not be provided twice`() {
            //given
            // when
            val exception = assertThrows<Throwable> {
                feature<Any, String> {
                    triggeredStream { _, _ -> Observable.never() }
                    onError { _ -> { it } }
                    startWith { Any() }
                    startWith { Any() }
                }
            }

            // then
            assertThat(exception.message, containsString("startWith"))
        }

        @Test
        fun `'stateEvents' must not be provided twice`() {
            //given
            // when
            val exception = assertThrows<Throwable> {
                feature<Any, String> {
                    triggeredStream { _, _ -> Observable.never() }
                    onError { _ -> { it } }
                    stateEvents { Observable.never() }
                    stateEvents { Observable.never() }
                }
            }

            // then
            assertThat(exception.message, containsString("stateEvents"))
        }

    }

    @Nested
    inner class CorrectSyntax {

        @Test
        fun `Configuration succeeds when 'triggeredStream' and 'onError' are provided`() {
            //given
            // when
            assertDoesNotThrow {
                feature<Any, String> {
                    triggeredStream { _, _ -> Observable.never() }
                    onError { _ -> { it } }
                }
            }

            // then
        }

        @Test
        fun `Configuration succeeds with 'triggeredStream' of existing Observable`() {
            //given
            // when
            assertDoesNotThrow {
                feature<Any, String> {
                    triggeredStream(Observable.never())
                    onError { _ -> { it } }
                }
            }

            // then
        }

        @Test
        fun `Configuration succeeds with 'triggeredSingle' of existing Single`() {
            //given
            // when
            assertDoesNotThrow {
                feature<Any, String> {
                    triggeredSingle(Single.never())
                    onError { _ -> { it } }
                }
            }

            // then
        }

        @Test
        fun `Configuration succeeds with 'triggeredSingle' instead of 'triggeredStream'`() {
            //given
            // when
            assertDoesNotThrow {
                feature<Any, String> {
                    triggeredSingle { _, _ -> Single.never() }
                    onError { _ -> { it } }
                }
            }

            // then
        }

        @Test
        fun `Configuration succeeds when 'startWith' is also provided`() {
            //given
            // when
            assertDoesNotThrow {
                feature<Any, String> {
                    triggeredStream { _, _ -> Observable.never() }
                    onError { _ -> { it } }
                    startWith { Any() }
                }
            }

            // then
        }

        @Test
        fun `Configuration succeeds when 'stateEvents' is also provided`() {
            //given
            // when
            assertDoesNotThrow {
                feature<Any, String> {
                    triggeredStream { _, _ -> Observable.never() }
                    onError { _ -> { it } }
                    stateEvents { Observable.never() }
                }
            }

            // then
        }

    }

    @Nested
    inner class FeatureExamples {

        @Test
        fun `Creation of an identity feature (with 'triggeredSingle')`() {
            //given

            // when
            assertDoesNotThrow {
                identityStringFeature()
            }
            // then
        }

        @Test
        fun `Creation of a no-op feature`() {
            //given
            val sut = feature<Any, String> {
                triggeredStream(Observable.never())
                onError { _ -> { it } }
            }
            sut.context.onNext("initial state")
            val result = sut.reducers.test()

            // when
            sut.externalEvent(Any())

            // then
            result.assertNoValues()
                    .assertNotTerminated()
        }
    }

    @Test
    fun `Identity feature emits external event as is when context is set, then reducers is subscribed, before the event occurs`() {
        //given
        val sut = identityStringFeature()
        sut.context.onNext("initial context")
        val result = sut.reducers.map { function -> function.invoke("that must be ignored") }.test()

        // when
        sut.externalEvent("a string")

        // then
        result.assertValue("a string")
                .assertNotTerminated()
    }

    @Test
    fun `Identify feature emits external event as is when reducers is subscribed, then context is set, before the event occurs`() {
        //given
        val sut = feature<String, String> {
            triggeredSingle { event, _ -> Single.just { _ -> event } }
            onError { _ -> { it } }
        }
        val result = sut.reducers.map { function -> function.invoke("current state") }.test()
        sut.context.onNext("initial state")

        // when
        sut.externalEvent("a string")

        // then
        result.assertValue("a string")
                .assertNotTerminated()
    }

    @Test
    fun `Identity feature does not emit on 1st external event if context not set at least once`() {
        //given
        val sut = identityStringFeature()
        val result = sut.reducers.test()

        // when
        sut.externalEvent("a string")

        // then
        result.assertNoValues()
                .assertNotTerminated()
    }

    @Test
    fun `Identity feature does not emit on 2nd external event if context not set at least once`() {
        //given
        val sut = identityStringFeature()
        val result = sut.reducers.test()
        sut.externalEvent("a string")

        // when
        sut.externalEvent("another string")

        // then
        result.assertNoValues()
                .assertNotTerminated()
    }

    @Test
    fun `Identity feature does not emit for 1st external event received before context is set`() {
        //given
        val sut = identityStringFeature()
        val result = sut.reducers.test()
        sut.externalEvent("a string")

        // when
        sut.context.onNext("an event")

        // then
        result.assertNoValues()
                .assertNotTerminated()
    }

    @Test
    fun `Identity feature does not emit for 2nd external event received before context is set`() {
        //given
        val sut = identityStringFeature()
        val result = sut.reducers.test()
        sut.externalEvent("a string")
        sut.externalEvent("another string")

        // when
        sut.context.onNext("an event")

        // then
        result.assertNoValues()
                .assertNotTerminated()
    }

    @Test
    fun `Identity feature with a start event emits start event`() {
        //given
        val sut = feature<String, String> {
            triggeredSingle { event, _ -> Single.just { _ -> event } }
            onError { _ -> { it } }
            startWith { "the start" }
        }
        sut.context.onNext("initial context")

        // when
        val result = sut.reducers.map { function -> function.invoke("that must be ignored") }.test()

        // then
        result.assertValue("the start")
                .assertNotTerminated()
    }

    @Test
    fun `Identity feature with a start event emits start event and subsequent event`() {
        //given
        val sut = feature<String, String> {
            triggeredSingle { event, _ -> Single.just { _ -> event } }
            onError { _ -> { it } }
            startWith { "the start" }
        }
        sut.context.onNext("initial context")
        val result = sut.reducers.map { function -> function.invoke("that must be ignored") }.test()

        // when
        sut.externalEvent("a string")

        // then
        result.assertValues("the start", "a string")
                .assertNotTerminated()
    }

    @Test
    fun `Identity feature with a state event emits state event when context is set, then reducers is subscribed`() {
        //given
        val sut = feature<String, String> {
            triggeredSingle { event, _ -> Single.just { _ -> event } }
            onError { _ -> { it } }
            stateEvents { it }
        }

        // when
        sut.context.onNext("initial context")
        val result = sut.reducers.map { function -> function.invoke("that must be ignored") }.test()

        // then
        result.assertValues("initial context")
                .assertNotTerminated()
    }

    @Test
    fun `Identity feature with a state event emits state event when reducers is subscribed, then context is set`() {
        //given
        val sut = feature<String, String> {
            triggeredSingle { event, _ -> Single.just { _ -> event } }
            onError { _ -> { it } }
            stateEvents { it }
        }

        // when
        val result = sut.reducers.map { function -> function.invoke("that must be ignored") }.test()
        sut.context.onNext("initial context")

        // then
        result.assertValues("initial context")
                .assertNotTerminated()
    }

    private fun identityStringFeature() = feature<String, String> {
        triggeredSingle { event, _ -> Single.just { _ -> event } }
        onError { _ -> { it } }
    }

}