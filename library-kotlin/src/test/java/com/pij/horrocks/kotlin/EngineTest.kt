package com.pij.horrocks.kotlin

import com.nhaarman.mockitokotlin2.*
import com.pij.horrocks.kotlin.Engine.Companion.subscribeSafely
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.slf4j.Logger

internal class EngineTest {

    @Nested
    inner class CompanionTest {
        @Test
        fun `subscribeSafely() calls display when it receives an item`() {
            // given
            val display = mock<(String) -> Unit>()
            val input = Observable.just("hello")

            // when
            input.subscribeSafely(display)

            // then
            verify(display).invoke("hello")
        }

        @Test
        fun `subscribeSafely() is disposed of when its input completes`() {
            // given
            val display = mock<(String) -> Unit>()
            val input = Observable.just("hello")

            // when
            val subscription = input.subscribeSafely(display)

            // then
            assertTrue(subscription.isDisposed)
        }

        @Test
        fun `subscribeSafely() does not fail if display does not fail`() {
            // given
            val display = mock<(String) -> Unit>()
            val input = BehaviorSubject.createDefault("hello")

            // when
            val subscription = input.subscribeSafely(display)

            // then
            assertFalse(subscription.isDisposed)
        }

        @Test
        fun `subscribeSafely() does not fail if display fails`() {
            // given
            val display = mock<(String) -> Unit> { on { invoke(any()) } doThrow IllegalStateException("ta da") }
            val input = BehaviorSubject.createDefault("hello")

            // when
            val subscription = input.subscribeSafely(display)

            // then
            assertFalse(subscription.isDisposed)
        }
    }

    @Test
    fun `Create an engine without a feature fails`() {
        // given

        // when
        assertThrows<IllegalArgumentException> {
            Engine.create("the initial", { this }, TestScheduler())
        }

        // then
    }

    private fun feature(reducers: Observable<Reducer<String>>) = mock<Feature<Boolean, String>> {
        on { context } doReturn PublishSubject.create()
        on { this.reducers } doReturn reducers
    }


    @Test
    fun `Create an engine without a single feature does not fail`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val feature = feature(reducers)

        // when
        Engine.create("the initial", { this }, TestScheduler(), feature)

        // then
    }

    @Test
    fun `Engine emits the initial state if feature emits nothing`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val sut = Engine.create("the initial", { this }, feature(reducers))

        // when
        val result = sut.states.test()

        // then
        result.assertValue("the initial")
                .assertNotTerminated()
    }

    @Test
    fun `Engine emits reducer's value provided by feature`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val sut = Engine.create("the initial", { this }, feature(reducers))
        val result = sut.states.test()

        // when
        reducers.onNext { "transformed" }

        // then
        result.assertValues("the initial", "transformed")
                .assertNotTerminated()
    }

    @Test
    fun `Engine logs reducer's value provided by feature`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val logger = mock<Logger>()
        val sut = Engine.create("the initial", { this }, logger, feature(reducers))
        sut.states.test()

        // when
        reducers.onNext { "transformed" }

        // then
        verify(logger).debug(argThat { contains("the initial") })
        verify(logger).debug(argThat { contains("transformed") })
    }

    @Test
    fun `Engine does not emit initialState if scheduler does not work`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val foreground = TestScheduler()
        val sut = Engine.create("the initial", { this }, foreground, feature(reducers))

        // when
        val result = sut.states.test()

        // then
        result.assertNoValues()
                .assertNotTerminated()
    }

    @Test
    fun `Engine emits initialState on the specified scheduler`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val foreground = TestScheduler()
        val sut = Engine.create("the initial", { this }, foreground, feature(reducers))
        val result = sut.states.test()

        // when
        foreground.triggerActions()

        // then
        result.assertValue("the initial")
                .assertNotTerminated()
    }

    @Test
    fun `Engine emits subsequent state on the specified scheduler`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val foreground = TestScheduler()
        val sut = Engine.create("the initial", { this }, foreground, feature(reducers))
        val result = sut.states.test()

        // when
        reducers.onNext { "transformed" }
        foreground.triggerActions()

        // then
        result.assertValues("the initial", "transformed")
                .assertNotTerminated()
    }

    @Test
    fun `Engine fails if feature fails`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val sut = Engine.create("the initial", { this }, feature(reducers))
        val result = sut.states.test()

        // when
        reducers.onError(IllegalStateException("ta da"))

        // then
        result.assertValues("the initial")
                .assertError(IllegalStateException::class.java)
    }

    @Test
    fun `Engine logs a warning if feature fails`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val logger = mock<Logger>()
        val sut = Engine.create("the initial", { this }, logger, feature(reducers))
        sut.states.test()

        // when
        reducers.onError(IllegalStateException("ta da"))

        // then
        verify(logger).warn(anyString(), any())
    }

    @Test
    fun `Engine fails if reducer fails`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val sut = Engine.create("the initial", { this }, feature(reducers))
        val result = sut.states.test()

        // when
        reducers.onNext { throw IllegalStateException("ta da") }

        // then
        result.assertValues("the initial")
                .assertError(IllegalStateException::class.java)
    }

    @Test
    fun `Engine logs a warning if reducer fails`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val logger = mock<Logger>()
        val sut = Engine.create("the initial", { this }, logger, feature(reducers))
        sut.states.test()

        // when
        reducers.onNext { throw IllegalStateException("ta da") }

        // then
        verify(logger).warn(anyString(), any())
    }

    @Test
    fun `Late subscriber to states receives initial state`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val sut = Engine.create("the initial", { this }, feature(reducers))

        // when
        val result = sut.states.test()

        // then
        result.assertValue("the initial")
                .assertNoErrors()
    }

    @Test
    fun `Late subscriber to states does not received non- initial state`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val sut = Engine.create("the initial", { this }, feature(reducers))
        reducers.onNext { "transformed" }

        // when
        val result = sut.states.test()

        // then
        result.assertNever("transformed")
                .assertNoErrors()
    }

    @Test
    fun `Late subscriber to states receives later state`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val sut = Engine.create("the initial", { this }, feature(reducers))
        reducers.onNext { "transformed" }
        val result = sut.states.test()

        // when
        reducers.onNext { "transformed2" }

        // then
        result.assertValues("the initial", "transformed2")
                .assertNoErrors()
    }

    @Test
    fun `Late 2nd subscriber receives last state`() {
        // given
        val reducers = PublishSubject.create<Reducer<String>>()
        val sut = Engine.create("the initial", { this }, feature(reducers))
        sut.states.test()
        reducers.onNext { "transformed" }

        // when
        val result = sut.states.test()

        // then
        result.assertValues("transformed")
                .assertNoErrors()
    }

}