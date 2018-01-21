/*
 * Copyright 2018, Chiswick Forest
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.pij.horrocks

import io.reactivex.Observable
import io.reactivex.functions.Function
import io.reactivex.observers.TestObserver
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*

/**
 *
 * Created on 17/11/2017.
 *
 * @author PierreJean
 */
class MultipleResultFeatureTest {

    @Test
    fun `result() emits no Result if there's no event`() {
        val sut = MultipleResultFeature<String, Int>(Function { Observable.just(Result { 0 }, Result { 0 }) }, SysoutLogger())
        val observer = sut.result().test()

        observer.assertNoValues()
                .assertNotComplete()
    }

    @Test
    fun `result() emits 2 Results if 1 event is triggered and the 'state modifier' emits 2 times`() {
        val sut = MultipleResultFeature<String, Int>(Function { Observable.just(Result { 0 }, Result { 0 }) }, SysoutLogger())
        val observer = sut.result().test()

        sut.trigger("some event")

        observer.assertValueCount(2)
                .assertNotComplete()
    }

    @Test
    fun `result() emits 4 Results if 2 events are triggered and the 'state modifier' emits 2 times`() {
        val sut = MultipleResultFeature<String, Int>(Function { Observable.just(Result { 0 }, Result { 0 }) }, SysoutLogger())
        val observer = sut.result().test()

        sut.trigger("some event")
        sut.trigger("some other event")

        observer.assertValueCount(4)
                .assertNotComplete()
    }

    @Test
    fun `results emitted produce state as defined by the 'state modifier'`() {
        val sut = MultipleResultFeature<String, Int>(Function {
            Observable.just(
                    Result { 0 },
                    Result { state -> state + it.length }
            )
        }, SysoutLogger())
        val observer: TestObserver<out Result<Int>> = sut.result().test()

        sut.trigger("12345678")

        val result1 = observer.values()[0]
        assertThat(result1.applyTo(1), equalTo(0))
        val result2 = observer.values()[1]
        assertThat(result2.applyTo(1), equalTo(9))
    }

    @Test
    fun `Logs event`() {
        val loggerMock = mock(Logger::class.java)
        val sut = MultipleResultFeature<String, Int>(Function { Observable.just(Result { 0 }, Result { 0 }) }, loggerMock)
        sut.result().test()

        sut.trigger("something")

        verify(loggerMock).print(ArgumentMatchers.any(), Mockito.argThat { it.contains("Received event") })
    }

    @Test
    fun `Logs results`() {
        val loggerMock = mock(Logger::class.java)
        val sut = MultipleResultFeature<String, Int>(Function { Observable.just(Result { 0 }, Result { 0 }) }, loggerMock)
        sut.result().test()

        sut.trigger("something")

        verify(loggerMock, times(2)).print(ArgumentMatchers.any(), Mockito.argThat { it.contains("Emitting result") })
    }
}

