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

package com.pij.horrocks.storage

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.*

/**
 *
 * Created on 06/10/2018.
 *
 * @author PierreJean
 */
class QueueStorageTest {

    @get:Rule
    val thrown: ExpectedException = ExpectedException.none()

    @Test
    fun `load() uses last of non-empty queue`() {
        // given
        val queue: Queue<String> = ArrayDeque<String>(listOf("hello", "Hello2"))
        assertThat(queue.peek(), equalTo("hello"))
        val sut = QueueStorage(queue)

        // when
        val result = sut.load()

        // then
        assertThat(result, equalTo("hello"))
    }

    @Test
    fun `load() throws if queue is empty`() {
        // given
        val sut = QueueStorage<String>(
                ArrayDeque(0))

        // when
        thrown.expect(Exception::class.java)
        sut.load()

        // then
    }

    @Test
    fun `save() adds to queue`() {
        // given
        val queue = ArrayDeque<String>(0)
        val sut = QueueStorage(queue)

        // when
        sut.save("hello")

        // then
        assertThat(queue.size, equalTo(1))
    }

    @Test
    fun `save() appends value to queue`() {
        // given
        val queue = ArrayDeque<String>(0)
        val sut = QueueStorage(queue)

        // when
        sut.save("hello")

        // then
        assertThat(queue.last, equalTo("hello"))
    }
}