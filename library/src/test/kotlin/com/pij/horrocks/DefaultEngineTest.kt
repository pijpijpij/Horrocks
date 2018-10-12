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

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.pij.horrocks.storage.MemoryStorage
import com.pij.horrocks.storage.Storage
import com.pij.utils.SysoutLogger
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Created on 17/11/2017.
 *
 * @author PierreJean
 */
class DefaultEngineTest {

    data class DummyState(val transient: Boolean, val nonTransient: Int)

    private lateinit var sut: DefaultEngine<DummyState, DummyState>

    @BeforeTest
    fun setUp() {
        sut = DefaultEngine(SysoutLogger())
    }

    private fun lengthCalculator(): ReducerCreator<String, DummyState> {
        return object : ReducerCreator<String, DummyState> {
            private val events: Subject<String> = PublishSubject.create()
            override fun trigger(input: String) = events.onNext(input)
            override fun reducers(): Observable<out Reducer<DummyState>> = events.map { input ->
                Reducer<DummyState> { it.copy(nonTransient = input.length) }
            }
        }
    }

    private fun accumulator(): ReducerCreator<Int, DummyState> {
        return object : ReducerCreator<Int, DummyState> {
            private val events: Subject<Int> = PublishSubject.create()
            override fun trigger(input: Int) = events.onNext(input)
            override fun reducers(): Observable<out Reducer<DummyState>> = events.map { input ->
                Reducer<DummyState> { it.copy(nonTransient = input + it.nonTransient) }
            }

        }
    }

    @Test
    fun `Engine with single feature retrieves initial state from storage`() {
        // given
        val mockStorage = mock<Storage<DummyState>>()
        whenever(mockStorage.load()).thenReturn(DummyState(false, 1))
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(mockStorage)
                .stateToModel { it }
                .creators(setOf(lengthCalculator()))
                .build()

        // when
        sut.runWith(configuration).test()

        // then
        verify(mockStorage).load()
    }

    @Test
    fun `Engine with single feature stores calculated state from initial state`() {
        // given
        val mockStorage = mock<Storage<DummyState>>()
        whenever(mockStorage.load()).thenReturn(DummyState(false, 1))
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(mockStorage)
                .stateToModel { it }
                .creators(setOf(lengthCalculator()))
                .build()

        // when
        sut.runWith(configuration).test()

        // then
        verify(mockStorage).save(DummyState(false, 1))
    }

    @Test
    fun `An event on a simple Feature stores calculated state`() {
        // given
        val mockStorage = mock<Storage<DummyState>>()
        whenever(mockStorage.load()).thenReturn(DummyState(false, 1))
        val reducerCreator = lengthCalculator()
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(mockStorage)
                .stateToModel { it }
                .creators(setOf(reducerCreator))
                .build()
        sut.runWith(configuration).test()

        // when
        reducerCreator.trigger("hello")

        // then
        verify(mockStorage).save(DummyState(false, 5))
    }

    @Test
    fun `Emits the initial state even without registered Features`() {
        // given
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 1)))
                .stateToModel { it }
                .creators(emptyList())
                .build()

        // when
        val observer = sut.runWith(configuration).test()

        // then
        observer.assertValue(DummyState(false, 1))
    }

    @Test
    fun `Emits the initial state with a simple Feature registered `() {
        val dummyReducerCreator: ReducerCreator<String, DummyState> = lengthCalculator()
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(dummyReducerCreator))
                .build()
        val observer = sut.runWith(configuration).test()

        observer.assertValue(DummyState(false, 23))
    }

    @Test
    fun `An event on a simple Feature emits a single model`() {
        // given
        val addN: ReducerCreator<Int, DummyState> = accumulator()
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(addN))
                .build()
        val observer = sut.runWith(configuration).test()

        // when
        addN.trigger(1)

        // then
        observer.assertValues(DummyState(false, 23), DummyState(false, 24))
    }

    @Test
    fun `Event on a Feature emitting 2 reducers per event emits 2 models`() {
        // given
        val addAtStartAndStop: ReducerCreator<Int, DummyState> = object : ReducerCreator<Int, DummyState> {
            private val events: Subject<Int> = PublishSubject.create()
            override fun trigger(input: Int) = events.onNext(input)
            override fun reducers(): Observable<out Reducer<DummyState>> = events.flatMap { input ->
                Observable.just(
                        Reducer<DummyState> { it.copy(nonTransient = input + it.nonTransient) },
                        Reducer { it.copy(nonTransient = 2 * input + it.nonTransient) }
                )
            }
        }

        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 1)))
                .stateToModel { it }
                .creators(setOf(addAtStartAndStop))
                .build()
        val observer = sut.runWith(configuration).test()

        // when
        addAtStartAndStop.trigger(1)

        // then
        observer.assertValues(DummyState(false, 1), DummyState(false, 2), DummyState(false, 4))
    }

    @Test
    fun `Fails when a feature fails to construct a Reducer`() {
        // given
        val reducerCreatorCannotConstructReducer: ReducerCreator<Any, DummyState> = object : ReducerCreator<Any, DummyState> {
            private val events: Subject<Any> = PublishSubject.create()
            override fun trigger(input: Any) = events.onNext(input)
            override fun reducers(): Observable<out Reducer<DummyState>> = events.map { throw IllegalStateException("zap") }
        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(reducerCreatorCannotConstructReducer))
                .build()
        val observer = sut.runWith(configuration).test()

        // when
        reducerCreatorCannotConstructReducer.trigger(1)

        // then
        observer.assertErrorMessage("zap")
    }

    @Test
    fun `Fails when a feature's reducer throws`() {
        // given
        val failingReducerCreator: ReducerCreator<Any, DummyState> = object : ReducerCreator<Any, DummyState> {
            private val events: Subject<Any> = PublishSubject.create()
            override fun trigger(input: Any) = events.onNext(input)
            override fun reducers(): Observable<out Reducer<DummyState>> = events.map { _ ->
                Reducer<DummyState> { throw IllegalStateException("zip") }
            }

        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(failingReducerCreator))
                .build()
        val observer = sut.runWith(configuration).test()

        // when
        failingReducerCreator.trigger(1)

        // then
        observer.assertErrorMessage("zip")
    }

    @Test
    fun `Transient property in initial State is not emitted`() {
        // given
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(true, 1)))
                .stateToModel { it }
                .creators(emptySet())
                .transientResetter { it -> it.copy(transient = false) }
                .build()

        // when
        val observer = sut.runWith(configuration).map(DummyState::transient).test()

        // then
        observer.assertValue(false)
    }

    @Test
    fun `Engine resets transient property set in 1st reducer when emitting 2nd reducer`() {
        // given
        val aReducerCreator: ReducerCreator<Any, DummyState> = object : ReducerCreator<Any, DummyState> {
            private val events: Subject<Any> = PublishSubject.create()
            override fun trigger(input: Any) = events.onNext(input)
            override fun reducers(): Observable<out Reducer<DummyState>> = events.flatMap { _ ->
                Observable.just(
                        Reducer<DummyState> { it.copy(transient = true) },
                        Reducer { it }
                )
            }
        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(true, 1)))
                .stateToModel { it }
                .creators(setOf(aReducerCreator))
                .transientResetter { it -> it.copy(transient = false) }
                .build()
        val observer = sut.runWith(configuration).map(DummyState::transient).test()

        // when
        aReducerCreator.trigger(1)

        // then
        observer.assertValues(false, true, false)
    }
}

