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
import org.junit.Before
import org.junit.Test

/**
 *
 * Created on 17/11/2017.
 *
 * @author PierreJean
 */
class DefaultEngineTest {

    data class DummyState(val transient: Boolean, val nonTransient: Int)

    private lateinit var sut: DefaultEngine<DummyState, DummyState>

    @Before
    fun setUp() {
        sut = DefaultEngine(SysoutLogger())
    }

    @Test
    fun `Emits the initial state even without registered Features`() {
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStore(DummyState(false, 1)))
                .stateToModel { it }
                .features(emptyList())
                .build()

        val observer = sut.runWith(configuration).test()

        observer.assertValue(DummyState(false, 1))
    }

    @Test
    fun `Emits the initial state when a simple Feature is registered `() {
        val dummyFeature: Feature<String, DummyState> = object : Feature<String, DummyState> {
            private val events: Subject<String> = PublishSubject.create()
            override fun trigger(input: String) = events.onNext(input)
            override fun result(): Observable<out Result<DummyState>> = events.map { input ->
                Result<DummyState> { it.copy(nonTransient = input.length) }
            }
        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStore(DummyState(false, 23)))
                .stateToModel { it }
                .features(setOf(dummyFeature))
                .build()
        val observer = sut.runWith(configuration).test()

        observer.assertValue(DummyState(false, 23))
    }

    @Test
    fun `An event on a simple Feature emits a single model`() {
        val addN: Feature<Int, DummyState> = object : Feature<Int, DummyState> {
            private val events: Subject<Int> = PublishSubject.create()
            override fun trigger(input: Int) = events.onNext(input)
            override fun result(): Observable<out Result<DummyState>> = events.map { input ->
                Result<DummyState> { it.copy(nonTransient = input + it.nonTransient) }
            }

        }
        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStore(DummyState(false, 23)))
                .stateToModel { it }
                .features(setOf(addN))
                .build()
        val observer = sut.runWith(configuration).test()

        addN.trigger(1)

        observer.assertValues(DummyState(false, 23), DummyState(false, 24))
    }

    @Test
    fun `Event on a Feature emitting 2 results per event emits 2 models`() {
        val addAtStartAndStop: Feature<Int, DummyState> = object : Feature<Int, DummyState> {
            private val events: Subject<Int> = PublishSubject.create()
            override fun trigger(input: Int) = events.onNext(input)
            override fun result(): Observable<out Result<DummyState>> = events.flatMap { input ->
                Observable.just(
                        Result<DummyState> { it.copy(nonTransient = input + it.nonTransient) },
                        Result { it.copy(nonTransient = 2 * input + it.nonTransient) }
                )
            }
        }

        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStore(DummyState(false, 1)))
                .stateToModel { it }
                .features(setOf(addAtStartAndStop))
                .build()
        val observer = sut.runWith(configuration).test()

        addAtStartAndStop.trigger(1)

        observer.assertValues(DummyState(false, 1), DummyState(false, 2), DummyState(false, 4))
    }

    @Test
    fun `Transient property in initial State is not emitted`() {

        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStore(DummyState(true, 1)))
                .stateToModel { it }
                .features(emptySet())
                .transientResetter { it -> it.copy(transient = false) }
                .build()
        val observer = sut.runWith(configuration).map(DummyState::transient).test()

        observer.assertValue(false)
    }

    @Test
    fun `Engine resets transient property set in 1st result when emitting 2nd result`() {
        val aFeature: Feature<Int, DummyState> = object : Feature<Int, DummyState> {
            private val events: Subject<Int> = PublishSubject.create()
            override fun trigger(input: Int) = events.onNext(input)
            override fun result(): Observable<out Result<DummyState>> = events.flatMap { _ ->
                Observable.just(
                        Result<DummyState> { it.copy(transient = true) },
                        Result { it }
                )
            }
        }

        val configuration = Configuration.builder<DummyState, DummyState>()
                .store(MemoryStore(DummyState(true, 1)))
                .stateToModel { it }
                .features(setOf(aFeature))
                .transientResetter { it -> it.copy(transient = false) }
                .build()
        val observer = sut.runWith(configuration).map(DummyState::transient).test()

        aFeature.trigger(1)

        observer.assertValues(false, true, false)
    }
}

