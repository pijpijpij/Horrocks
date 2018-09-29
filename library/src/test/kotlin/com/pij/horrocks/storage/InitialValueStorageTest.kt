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

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.concurrent.Callable
import kotlin.test.fail

/**
 *
 * Created on 07/10/2018.
 *
 * @author PierreJean
 */
class InitialValueStorageTest {

    @get:Rule
    val thrown: ExpectedException = ExpectedException.none()

    @Test
    fun `load() tries to use decorated value`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        whenever(mockDecorated.load()).thenReturn("ta da!")
        val sut = InitialValueStorage(mockDecorated, Callable { "hello" })

        // when
        val result = sut.load()

        // then
        assertThat(result, equalTo("ta da!"))
    }

    @Test
    fun `load() uses initial value the 1st time decorated throws`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        whenever(mockDecorated.load()).thenThrow(IllegalArgumentException("ta da!"))
        val sut = InitialValueStorage(mockDecorated, Callable { "hello" })

        // when
        val result = sut.load()

        // then
        assertThat(result, equalTo("hello"))
    }

    @Test
    fun `load() throws the 2nd time decorated throws`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        whenever(mockDecorated.load()).thenThrow(IllegalArgumentException("ta da!"))
        val sut = InitialValueStorage(mockDecorated, Callable { "hello" })
        sut.load()

        // when
        thrown.expectMessage("ta da!")
        sut.load()

        // then
    }

    @Test
    fun `save() passes through to the decorated`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        val sut = InitialValueStorage(mockDecorated, Callable<String> { fail("this shouldn't be called") })
        sut.load()

        // when
        sut.save("some string")

        // then
        verify(mockDecorated).save("some string")
    }

    @Test
    fun `save() throws if decorated throws`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        whenever(mockDecorated.save("some string")).thenThrow(IllegalArgumentException("ta da!"))
        val sut = InitialValueStorage(mockDecorated, Callable<String> { fail("this shouldn't be called") })
        sut.load()

        // when
        thrown.expectMessage("ta da!")
        sut.save("some string")

        // then
    }

}