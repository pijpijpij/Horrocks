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

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.test.Test

/**
 *
 * Created on 03/10/2018.
 *
 * @author PierreJean
 */
class StorageToolkitTest {

    @Mock
    private lateinit var mockSerializer: Serializer<String>
    @Mock
    private lateinit var mockDeserializer: Deserializer<String>
    @Mock
    private lateinit var mockStreamAccessor: StreamAccessor

    @Before
    fun setUp() {
        initMocks(this)
    }

    private fun createDefaultSut() =
            StorageToolkit(mockSerializer, mockDeserializer, mockStreamAccessor)

    @Test
    fun `openForRead() gets a stream from stream accessor`() {
        // given
        val sut = createDefaultSut()

        // when
        sut.openForRead()

        // then
        verify(mockStreamAccessor).openForRead()
    }

    @Test
    fun `Closing openForRead()'s stream closes stream accessor's input stream`() {
        // given
        val mockStream = mock<InputStream>()
        whenever(mockStreamAccessor.openForRead()).thenReturn(mockStream)
        val sut = createDefaultSut()
        val stream = sut.openForRead()

        // when
        stream.close()

        // then
        verify(mockStream).close()
    }

    @Test
    fun `Data read directly from openForRead()'s stream is identical to data on stream accessor's input stream`() {
        // given
        val accessorStream = ByteArrayInputStream("hello!".toByteArray(Charset.defaultCharset()))
        whenever(mockStreamAccessor.openForRead()).thenReturn(accessorStream)
        val sut = createDefaultSut()
        val stream = sut.openForRead()

        // when
        val buffer = ByteArray(10)
        val count = stream.read(buffer)
        val result = String(buffer, 0, count)

        // then
        assertThat(result, equalTo("hello!"))
    }

    @Test
    fun `openForAppend() gets a stream from stream accessor`() {
        // given
        val sut = createDefaultSut()

        // when
        sut.openForAppend()

        // then
        verify(mockStreamAccessor).openForAppend()
    }

    @Test
    fun `Closing openForAppend()'s stream closes stream accessor's output stream`() {
        // given
        val mockStream = mock<OutputStream>()
        whenever(mockStreamAccessor.openForAppend()).thenReturn(mockStream)
        val sut = createDefaultSut()
        val stream = sut.openForAppend()

        // when
        stream.close()

        // then
        verify(mockStream).close()
    }

    @Test
    fun `Data written directly to openForAppend()'s stream appears on stream accessor's output stream`() {
        // given
        val accessorStream = ByteArrayOutputStream()
        whenever(mockStreamAccessor.openForAppend()).thenReturn(accessorStream)
        val sut = createDefaultSut()
        val stream = sut.openForAppend()

        // when
        stream.write("hello!".toByteArray(Charset.defaultCharset()))
        stream.close()

        // then
        val result = accessorStream.toByteArray().toString(Charset.defaultCharset())
        assertThat(result, equalTo("hello!"))
    }

    @Test
    fun `readLast() converts data read from the provided stream`() {
        // given
        val stream = ByteArrayInputStream("hello!".toByteArray(Charset.defaultCharset()))
        whenever(mockDeserializer.invoke(stream)).thenReturn("zig zag")
        val sut = createDefaultSut()

        // when
        val result: String = sut.readLast(stream)

        // then
        assertThat(result, equalTo("zig zag"))
    }

    @Test
    fun `write() converts data written onto the provided stream`() {
        // given
        val stream = ByteArrayOutputStream()
        doAnswer { stream.write("zip zap".toByteArray(Charset.defaultCharset())) }
                .whenever(mockSerializer).invoke("hello!", stream)
        val sut = createDefaultSut()

        // when
        sut.write("hello!", stream)

        // then
        val result = stream.toByteArray().toString(Charset.defaultCharset())
        assertThat(result, equalTo("zip zap"))
    }

    @Test
    fun `openForRead(), readLast() reads from the provided stream`() {
        // given
        val accessorStream = ByteArrayInputStream("hello!".toByteArray(Charset.defaultCharset()))
        whenever(mockStreamAccessor.openForRead()).thenReturn(accessorStream)
        val sut = StorageToolkit(mockSerializer, { "hello!" }, mockStreamAccessor)

        // when
        val stream = sut.openForRead()
        val result: String = sut.readLast(stream)

        // then
        assertThat(result, equalTo("hello!"))
    }

    @Test
    fun `openForAppend(), write() then close() writes onto the provided stream`() {
        // given
        val accessorStream = ByteArrayOutputStream()
        whenever(mockStreamAccessor.openForAppend()).thenReturn(accessorStream)
        val sut = StorageToolkit(
                { data, output -> output.write(data.toByteArray(Charset.defaultCharset())) },
                mockDeserializer, mockStreamAccessor)

        // when
        val stream = sut.openForAppend()
        sut.write("hello!", stream)
        sut.close(stream)

        // then
        val result = accessorStream.toByteArray().toString(Charset.defaultCharset())
        assertThat(result, equalTo("hello!"))
    }
}