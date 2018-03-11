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

    @Test
    fun `Emits the initial state even without registered Features`() {
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 1)))
                .stateToModel { it }
                .creators(emptyList())
                .build()

        val observer = sut.runWith(configuration).test()

        observer.assertValue(DummyState(false, 1))
    }

    @Test
    fun `Emits the initial state when a simple Feature is registered `() {
        val dummyReducerCreator: ReducerCreator<String, DummyState> = object : ReducerCreator<String, DummyState> {
            private val events: Subject<String> = PublishSubject.create()
            override fun trigger(input: String) = events.onNext(input)
            override fun reducers(): Observable<out Reducer<DummyState>> = events.map { input ->
                Reducer<DummyState> { it.copy(nonTransient = input.length) }
            }
        }
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
        val addN: ReducerCreator<Int, DummyState> = object : ReducerCreator<Int, DummyState> {
            private val events: Subject<Int> = PublishSubject.create()
            override fun trigger(input: Int) = events.onNext(input)
            override fun reducers(): Observable<out Reducer<DummyState>> = events.map { input ->
                Reducer<DummyState> { it.copy(nonTransient = input + it.nonTransient) }
            }

        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(addN))
                .build()
        val observer = sut.runWith(configuration).test()

        addN.trigger(1)

        observer.assertValues(DummyState(false, 23), DummyState(false, 24))
    }

    @Test
    fun `Event on a Feature emitting 2 reducers per event emits 2 models`() {
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

        addAtStartAndStop.trigger(1)

        observer.assertValues(DummyState(false, 1), DummyState(false, 2), DummyState(false, 4))
    }

    @Test
    fun `Fails when a feature fails to construct a Reducer`() {
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

        reducerCreatorCannotConstructReducer.trigger(1)

        observer.assertErrorMessage("zap")
    }

    @Test
    fun `Fails when a feature's reducer throws`() {
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

        failingReducerCreator.trigger(1)

        observer.assertErrorMessage("zip")
    }

    @Test
    fun `Transient property in initial State is not emitted`() {

        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(true, 1)))
                .stateToModel { it }
                .creators(emptySet())
                .transientResetter { it -> it.copy(transient = false) }
                .build()
        val observer = sut.runWith(configuration).map(DummyState::transient).test()

        observer.assertValue(false)
    }

    @Test
    fun `Engine resets transient property set in 1st reducer when emitting 2nd reducer`() {
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

        aReducerCreator.trigger(1)

        observer.assertValues(false, true, false)
    }
}

