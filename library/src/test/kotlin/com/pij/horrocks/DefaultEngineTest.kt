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

import com.pij.utils.SysoutLogger
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
        val dummyReducerCreator: TriggeredReducerCreator<String, DummyState> = object : TriggeredReducerCreator<String, DummyState> {
            private val events: Subject<String> = PublishSubject.create()
            override fun trigger(input: String) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.map { input ->
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
        val addN: TriggeredReducerCreator<Int, DummyState> = object : TriggeredReducerCreator<Int, DummyState> {
            private val events: Subject<Int> = PublishSubject.create()
            override fun trigger(input: Int) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.map { input ->
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
        val addAtStartAndStop: TriggeredReducerCreator<Int, DummyState> = object : TriggeredReducerCreator<Int, DummyState> {
            private val events: Subject<Int> = PublishSubject.create()
            override fun trigger(input: Int) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.flatMap { input ->
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
    fun `Same consecutive state is emitted 2ce with the default state filter`() {
        val same: TriggeredReducerCreator<Any, DummyState> = sameStateCreator()
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(same))
                .build()
        val observer = sut.runWith(configuration).test()

        same.trigger(Any())

        observer.assertValueCount(2)
    }

    @Test
    fun `Same consecutive state is emitted 1ce with 'same' state filter`() {
        val same: TriggeredReducerCreator<Any, DummyState> = sameStateCreator()
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(same))
                .stateFilter { left, right -> left == right }
                .build()
        val observer = sut.runWith(configuration).test()

        same.trigger(Any())

        observer.assertValueCount(1)
    }

    private fun sameStateCreator(): TriggeredReducerCreator<Any, DummyState> {
        return object : TriggeredReducerCreator<Any, DummyState> {
            private val events: Subject<Any> = PublishSubject.create()
            override fun trigger(input: Any) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.map {
                Reducer<DummyState> { state -> state }
            }
        }
    }

    @Test
    fun `Does not fail when a feature fails to construct a Reducer and the default error reducer is used`() {
        // given
        val reducerCreatorCannotConstructReducer = triggeredReducerCreatorCannotConstructReducer()
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(reducerCreatorCannotConstructReducer))
                .build()
        val observer = sut.runWith(configuration).test()

        // when
        reducerCreatorCannotConstructReducer.trigger(1)

        // then
        observer.assertNoErrors()
    }

    @Test
    fun `Emits a state when a feature fails to construct a Reducer and the default error reducer is used`() {
        // given
        val reducerCreatorCannotConstructReducer = triggeredReducerCreatorCannotConstructReducer()
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(reducerCreatorCannotConstructReducer))
                .build()
        val observer = sut.runWith(configuration).test()

        // when
        reducerCreatorCannotConstructReducer.trigger(1)

        // then
        observer.assertValueCount(2)
    }

    @Test
    fun `Emits the same state again when a feature fails to construct a Reducer and the default error reducer is used`() {
        // given
        val reducerCreatorCannotConstructReducer = triggeredReducerCreatorCannotConstructReducer()
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(reducerCreatorCannotConstructReducer))
                .build()
        val observer = sut.runWith(configuration).distinctUntilChanged().test()

        // when
        reducerCreatorCannotConstructReducer.trigger(1)

        // then
        observer.assertValueCount(1)
    }

    @Test
    fun `Emits with altered state when a feature fails to construct a Reducer and a custom error reducer is used`() {
        // given
        val reducerCreatorCannotConstructReducer = triggeredReducerCreatorCannotConstructReducer()
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(reducerCreatorCannotConstructReducer))
                .errorReducerFactory { Reducer { current -> current.copy(transient = true) } }
                .build()
        val observer = sut.runWith(configuration).test()

        // when
        reducerCreatorCannotConstructReducer.trigger(1)

        // then
        observer.assertValueAt(1) { it.transient }
    }

    private fun triggeredReducerCreatorCannotConstructReducer(): TriggeredReducerCreator<Any, DummyState> {
        return object : TriggeredReducerCreator<Any, DummyState> {
            private val events: Subject<Any> = PublishSubject.create()
            override fun trigger(input: Any) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.map { throw IllegalStateException("zap") }
        }
    }

    @Test
    fun `Fails when a feature's reducer throws`() {
        val failingReducerCreator: TriggeredReducerCreator<Any, DummyState> = object : TriggeredReducerCreator<Any, DummyState> {
            private val events: Subject<Any> = PublishSubject.create()
            override fun trigger(input: Any) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.map {
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
        val aReducerCreator: TriggeredReducerCreator<Any, DummyState> = object : TriggeredReducerCreator<Any, DummyState> {
            private val events: Subject<Any> = PublishSubject.create()
            override fun trigger(input: Any) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.flatMap { _ ->
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

    @Test
    fun `Re-emits the last emitted state for a new Subscriber`() {
        // given
        val dummyReducerCreator: TriggeredReducerCreator<String, DummyState> = object : TriggeredReducerCreator<String, DummyState> {
            private val events: Subject<String> = PublishSubject.create()
            override fun trigger(input: String) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.map { input ->
                Reducer<DummyState> { it.copy(nonTransient = input.length) }
            }
        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(dummyReducerCreator))
                .build()
        val states = sut.runWith(configuration)
        states.test()
        dummyReducerCreator.trigger("some text with a specific length")

        // when
        val observer = states.test()

        // then
        observer.assertValue(DummyState(transient = false, nonTransient = 32))
    }

    @Test
    fun `Emits the initial state 2ce for 2 subscribers`() {
        // given
        val dummyReducerCreator: TriggeredReducerCreator<String, DummyState> = object : TriggeredReducerCreator<String, DummyState> {
            private val events: Subject<String> = PublishSubject.create()
            override fun trigger(input: String) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.map { input ->
                Reducer<DummyState> { it.copy(nonTransient = input.length) }
            }
        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(dummyReducerCreator))
                .build()
        val states = sut.runWith(configuration)

        // when
        val observer1 = states.test()
        val observer2 = states.test()

        // then
        observer1.assertValue(DummyState(false, 23))
        observer2.assertValue(DummyState(false, 23))
    }

    @Test
    fun `Emits the same (non-initial) state 2ce for 2 subscribers`() {
        // given
        val dummyReducerCreator: TriggeredReducerCreator<String, DummyState> = object : TriggeredReducerCreator<String, DummyState> {
            private val events: Subject<String> = PublishSubject.create()
            override fun trigger(input: String) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.map { input ->
                Reducer<DummyState> { it.copy(nonTransient = input.length) }
            }
        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(dummyReducerCreator))
                .build()
        val states = sut.runWith(configuration)

        // when
        val observer1 = states.test()
        val observer2 = states.test()
        dummyReducerCreator.trigger("text of some specific length")

        // then
        observer1.assertValues(DummyState(false, 23), DummyState(false, 28))
        observer2.assertValues(DummyState(false, 23), DummyState(false, 28))
    }

    @Test
    fun `Does not execute the same feature 2ce for 2 subscribers`() {
        // given
        var callCount = 0
        val dummyReducerCreator: TriggeredReducerCreator<String, DummyState> = object : TriggeredReducerCreator<String, DummyState> {
            private val events: Subject<String> = PublishSubject.create()
            override fun trigger(input: String) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.map { input ->
                callCount++
                Reducer<DummyState> { it.copy(nonTransient = input.length) }
            }
        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(dummyReducerCreator))
                .build()
        val states = sut.runWith(configuration)

        // when
        states.test()
        states.test()
        dummyReducerCreator.trigger("some text")

        // then
        assertEquals(1, callCount)
    }

    @Test
    fun `Does not execute the last Reducer 2ce for 2 subscribers`() {
        // given
        var callCount = 0
        val dummyReducerCreator: TriggeredReducerCreator<String, DummyState> = object : TriggeredReducerCreator<String, DummyState> {
            private val events: Subject<String> = PublishSubject.create()
            override fun trigger(input: String) = events.onNext(input)
            override fun reducers(): Observable<Reducer<DummyState>> = events.map { input ->
                Reducer<DummyState> { it ->
                    callCount++
                    it.copy(nonTransient = input.length)
                }
            }
        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStorage(DummyState(false, 23)))
                .stateToModel { it }
                .creators(setOf(dummyReducerCreator))
                .build()
        val states = sut.runWith(configuration)

        // when
        states.test()
        states.test()
        dummyReducerCreator.trigger("some text")

        // then
        assertEquals(1, callCount)
    }

}

