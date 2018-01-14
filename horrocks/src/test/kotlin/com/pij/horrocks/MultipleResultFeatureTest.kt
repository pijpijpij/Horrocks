package com.pij.horrocks

import io.reactivex.Observable
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
class MultipleResultFeatureTest {

    @Test
    fun `result() emits no Result if there's no event`() {
        val sut = MultipleResultFeature<String, Int>(Function { Observable.just(Result { 0 }, Result { 0 }) })
        val observer = sut.result().test()

        observer.assertNoValues()
        observer.assertNotComplete()
    }

    @Test
    fun `result() emits 2 Results if 1 event is triggered and the 'state modifier' emits 2 times`() {
        val sut = MultipleResultFeature<String, Int>(Function { Observable.just(Result { 0 }, Result { 0 }) })
        val observer = sut.result().test()

        sut.trigger("some event")

        observer.assertValueCount(2)
        observer.assertNotComplete()
    }

    @Test
    fun `result() emits 4 Results if 2 events are triggered and the 'state modifier' emits 2 times`() {
        val sut = MultipleResultFeature<String, Int>(Function { Observable.just(Result { 0 }, Result { 0 }) })
        val observer = sut.result().test()

        sut.trigger("some event")
        sut.trigger("some other event")

        observer.assertValueCount(4)
        observer.assertNotComplete()
    }

    @Test
    fun `results emitted produce state as defined by the 'state modifier'`() {
        val sut = MultipleResultFeature<String, Int>(Function {
            Observable.just(
                    Result { 0 },
                    Result { state -> state + it.length }
            )
        })
        val observer: TestObserver<out Result<Int>> = sut.result().test()

        sut.trigger("12345678")

        val result1 = observer.values()[0]
        assertThat(result1.applyTo(1), equalTo(0))
        val result2 = observer.values()[1]
        assertThat(result2.applyTo(1), equalTo(9))
    }
}

