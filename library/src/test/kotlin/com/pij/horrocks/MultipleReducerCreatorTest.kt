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

import com.pij.utils.Logger
import com.pij.utils.SysoutLogger
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.Mockito.*
import kotlin.test.Test


/**
 *
 * Created on 17/11/2017.
 *
 * @author PierreJean
 */
class MultipleReducerCreatorTest {

    @Test
    fun `reducer() emits no Reducer if there's no event`() {
        val sut = MultipleReducerCreator<String, Int>(AsyncInteraction { Observable.just(Reducer { 0 }, Reducer { 0 }) }, SysoutLogger())
        val observer = sut.reducers().test()

        observer.assertNoValues()
                .assertNotComplete()
    }

    @Test
    fun `reducer() emits 2 Reducers if 1 event is triggered and the 'state modifier' emits 2 times`() {
        val sut = MultipleReducerCreator<String, Int>(AsyncInteraction { Observable.just(Reducer { 0 }, Reducer { 0 }) }, SysoutLogger())
        val observer = sut.reducers().test()

        sut.trigger("some event")

        observer.assertValueCount(2)
                .assertNotComplete()
    }

    @Test
    fun `reducer() emits 4 Reducers if 2 events are triggered and the 'state modifier' emits 2 times`() {
        val sut = MultipleReducerCreator<String, Int>(AsyncInteraction { Observable.just(Reducer { 0 }, Reducer { 0 }) }, SysoutLogger())
        val observer = sut.reducers().test()

        sut.trigger("some event")
        sut.trigger("some other event")

        observer.assertValueCount(4)
                .assertNotComplete()
    }

    @Test
    fun `reducers emitted produce state as defined by the 'state modifier'`() {
        val sut = MultipleReducerCreator<String, Int>(AsyncInteraction {
            Observable.just(
                    Reducer { 0 },
                    Reducer { state -> state + it.length }
            )
        }, SysoutLogger())
        val observer: TestObserver<out Reducer<Int>> = sut.reducers().test()

        sut.trigger("12345678")

        val result1 = observer.values()[0]
        assertThat(result1.reduce(1), equalTo(0))
        val result2 = observer.values()[1]
        assertThat(result2.reduce(1), equalTo(9))
    }

    @Test
    fun `Logs event`() {
        val loggerMock = mock(Logger::class.java)
        val sut = MultipleReducerCreator<String, Int>(AsyncInteraction { Observable.just(Reducer { 0 }, Reducer { 0 }) }, loggerMock)
        sut.reducers().test()

        sut.trigger("something")

        verify(loggerMock).print(any(), contains("Received event"), eq("something"))
    }

    @Test
    fun `Logs reducers`() {
        val loggerMock = mock(Logger::class.java)
        val sut = MultipleReducerCreator<String, Int>(AsyncInteraction { Observable.just(Reducer { 0 }, Reducer { 0 }) }, loggerMock)
        sut.reducers().test()

        sut.trigger("something")

        verify(loggerMock, times(2)).print(any(), contains("Emitting reducer"), any())
    }
}

