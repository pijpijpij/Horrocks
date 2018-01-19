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
class EngineTest {

    private lateinit var sut: DefaultEngine<String, String>

    @Before
    fun setUp() {
        sut = DefaultEngine(SysoutLogger())
    }

    @Test
    fun `Emits the initial state even without registered Features`() {
        val configuration = Configuration.builder<String, String>()
                .store(MemoryStore("initial!"))
                .stateToModel { it }
                .features(emptyList())
                .build()

        val observer = sut.runWith(configuration).test()

        observer.assertValue("initial!")
    }

    @Test
    fun `Emits the initial state when a simple Feature is registered `() {
        val dummyFeature: Feature<String, String> = object : Feature<String, String> {
            private val events: Subject<String> = PublishSubject.create()
            override fun trigger(input: String) = events.onNext(input)
            override fun result(): Observable<out Result<String>> = events.map { input -> Result<String> { it + input } }
        }
        val configuration = Configuration.builder<String, String>()
                .store(MemoryStore("initial!"))
                .stateToModel { it }
                .features(setOf(dummyFeature))
                .build()
        val observer = sut.runWith(configuration).test()

        observer.assertValue("initial!")
    }

    @Test
    fun `An event on a simple Feature emits a single model`() {
        val addNAsCharacters: Feature<Int, String> = object : Feature<Int, String> {
            private val events: Subject<Int> = PublishSubject.create()
            override fun trigger(input: Int) = events.onNext(input)
            override fun result(): Observable<out Result<String>> = events.map { input ->
                Result<String> { it + input.toString() }
            }

        }
        val configuration = Configuration.builder<String, String>()
                .store(MemoryStore("initial!"))
                .stateToModel { it }
                .features(setOf(addNAsCharacters))
                .build()
        val observer = sut.runWith(configuration).test()

        addNAsCharacters.trigger(1)

        observer.assertValues("initial!", "initial!1")
    }

    @Test
    fun `Event on Feature emitting 2 results per event emits 2 models`() {
        val addWithStartAndStop: Feature<Int, String> = object : Feature<Int, String> {
            private val events: Subject<Int> = PublishSubject.create()
            override fun trigger(input: Int) = events.onNext(input)
            override fun result(): Observable<out Result<String>> = events.flatMap { input ->
                Observable.just(
                        Result { it.length.toString() },
                        Result<String> { it + input.toString() }
                )
            }
        }

        val configuration = Configuration.builder<String, String>()
                .store(MemoryStore("initial!"))
                .stateToModel { it }
                .features(setOf(addWithStartAndStop))
                .build()
        val observer = sut.runWith(configuration).test()

        addWithStartAndStop.trigger(1)

        observer.assertValues("initial!", "8", "81")
    }

}

