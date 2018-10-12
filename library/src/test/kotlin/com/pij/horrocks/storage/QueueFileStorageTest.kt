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
import org.junit.rules.TemporaryFolder
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 *
 * Created on 07/10/2018.
 *
 * @author PierreJean
 */
class QueueFileStorageTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun `Construction does not create file`() {
        //given
        val file = folder.newFile()
        assertThat(file.delete(), equalTo(true))
        val serializer: Serializer<String> = { data, output -> OutputStreamWriter(output).append(data) }
        val deserializer: Deserializer<String> = { input -> InputStreamReader(input).readText() }

        // when
        QueueFileStorage(file, serializer, deserializer)

        // then
        assertThat(file.exists(), equalTo(false))
    }

    @Test
    fun `save() creates file`() {
        //given
        val file = folder.newFile()
        assertThat(file.delete(), equalTo(true))
        val serializer: Serializer<String> = { data, output -> OutputStreamWriter(output).append(data) }
        val deserializer: Deserializer<String> = { input -> InputStreamReader(input).readText() }
        val sut = QueueFileStorage(file, serializer, deserializer)
        assertThat(file.exists(), equalTo(false))

        // when
        sut.save("hello")

        // then
        assertThat(file.exists(), equalTo(true))
    }
}