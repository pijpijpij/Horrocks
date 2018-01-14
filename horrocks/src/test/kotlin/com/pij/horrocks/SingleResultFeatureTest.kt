package com.pij.horrocks

import io.reactivex.functions.Function
import io.reactivex.observers.TestObserver
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 *
 * Created on 17/11/2017.
 *
 * @author PierreJean
 */
class SingleResultFeatureTest {

    @Test
    fun `result() emits no Result if there's no event`() {
        val sut = SingleResultFeature<String, Int>(Function { Result { 0 } })
        val observer = sut.result().test()

        observer.assertNoValues()
        observer.assertNotComplete()
    }

    @Test
    fun `result() emits 1 Result if 1 event is triggered`() {
        val sut = SingleResultFeature<String, Int>(Function { Result { 0 } })
        val observer = sut.result().test()

        sut.trigger("some event")

        observer.assertValueCount(1)
        observer.assertNotComplete()
    }

    @Test
    fun `result() emits 2 Results if 2 events are triggered`() {
        val sut = SingleResultFeature<String, Int>(Function { Result { 0 } })
        val observer = sut.result().test()

        sut.trigger("some event")
        sut.trigger("some other event")

        observer.assertValueCount(2)
        observer.assertNotComplete()
    }

    @Test
    fun `result() provides the function passed at construction`() {
        val sut = SingleResultFeature<String, Int>(Function { Result { state -> state + it.length } })
        val observer: TestObserver<out Result<Int>> = sut.result().test()

        sut.trigger("12345678")

        val result = observer.values()[0]
        assertThat(result.applyTo(1), equalTo(9))
    }
}

