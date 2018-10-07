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

    private fun failedTestInitialValue() =
            Callable<String> { fail("Should not be calling the default initial value") }

    @Test
    fun `1st call to load() tries to use decorated value`() {
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
    fun `1st call to load() does no use default value if decorated Storage works`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        whenever(mockDecorated.load()).thenReturn("ta da!")
        val sut = InitialValueStorage(mockDecorated, failedTestInitialValue())

        // when
        sut.load()

        // then
    }

    @Test
    fun `1st call to load() uses initial value when decorated Storage throws`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        whenever(mockDecorated.load()).thenThrow(IllegalArgumentException("rat!"))
        val sut = InitialValueStorage(mockDecorated, Callable { "hello" })

        // when
        val result = sut.load()

        // then
        assertThat(result, equalTo("hello"))
    }

    @Test
    fun `2nd call to load() returns decorated Storage's value`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        whenever(mockDecorated.load()).thenReturn("ta da!")
        val sut = InitialValueStorage(mockDecorated, failedTestInitialValue())

        // when
        val result = sut.load()

        // then
        assertThat(result, equalTo("ta da!"))
    }

    @Test
    fun `2nd call to load() throws whatever decorated Storage throws`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        whenever(mockDecorated.load()).thenReturn("Should not see this")
                .thenThrow(IllegalArgumentException("ta da!"))
        val sut = InitialValueStorage(mockDecorated, failedTestInitialValue())
        sut.load()

        // when
        thrown.expectMessage("ta da!")
        sut.load()

        // then
    }

    @Test
    fun `2nd call to load() does no use default value if decorated Storage works`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        whenever(mockDecorated.load()).thenReturn("ta da!")
        val sut = InitialValueStorage(mockDecorated, failedTestInitialValue())
        sut.load()

        // when
        sut.load()

        // then
    }

    @Test
    fun `save() passes through to the decorated Storage`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        val sut = InitialValueStorage(mockDecorated, failedTestInitialValue())
        sut.load()

        // when
        sut.save("some string")

        // then
        verify(mockDecorated).save("some string")
    }

    @Test
    fun `save() throws if decorated Storage throws`() {
        //given
        val mockDecorated = mock<Storage<String>>()
        whenever(mockDecorated.save("some string")).thenThrow(IllegalArgumentException("ta da!"))
        val sut = InitialValueStorage(mockDecorated, failedTestInitialValue())
        sut.load()

        // when
        thrown.expectMessage("ta da!")
        sut.save("some string")

        // then
    }

}