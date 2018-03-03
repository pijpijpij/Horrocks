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

import io.reactivex.functions.Function
import io.reactivex.observers.TestObserver
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mockito

/**
 *
 * Created on 17/11/2017.
 *
 * @author PierreJean
 */
class SingleResultFeatureTest {

    @Test
    fun `result() emits no Result if there's no event`() {
        val sut = SingleResultFeature<String, Int>(Function { ResultReducer { 0 } }, SysoutLogger())
        val observer = sut.results().test()

        observer.assertNoValues()
        observer.assertNotComplete()
    }

    @Test
    fun `result() emits 1 Result if 1 event is triggered`() {
        val sut = SingleResultFeature<String, Int>(Function { ResultReducer { 0 } }, SysoutLogger())
        val observer = sut.results().test()

        sut.trigger("some event")

        observer.assertValueCount(1)
        observer.assertNotComplete()
    }

    @Test
    fun `result() emits 2 Results if 2 events are triggered`() {
        val sut = SingleResultFeature<String, Int>(Function { ResultReducer { 0 } }, SysoutLogger())
        val observer = sut.results().test()

        sut.trigger("some event")
        sut.trigger("some other event")

        observer.assertValueCount(2)
        observer.assertNotComplete()
    }

    @Test
    fun `result() provides the function passed at construction`() {
        val sut = SingleResultFeature<String, Int>(Function { ResultReducer { state -> state + it.length } }, SysoutLogger())
        val observer: TestObserver<out ResultReducer<Int>> = sut.results().test()

        sut.trigger("12345678")

        val result = observer.values()[0]
        assertThat(result.applyTo(1), equalTo(9))
    }

    @Test
    fun `Logs event`() {
        val loggerMock = Mockito.mock(Logger::class.java)
        val sut = SingleResultFeature<String, Int>(Function { ResultReducer { 0 } }, loggerMock)
        sut.results().test()

        sut.trigger("something")

        Mockito.verify(loggerMock).print(any(), argThat { it.contains("Received event") })
    }

    @Test
    fun `Logs results`() {
        val loggerMock = Mockito.mock(Logger::class.java)
        val sut = SingleResultFeature<String, Int>(Function { ResultReducer { 0 } }, loggerMock)
        sut.results().test()

        sut.trigger("something")

        Mockito.verify(loggerMock).print(any(), argThat { it.contains("Emitting result") })
    }
}

