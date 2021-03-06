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

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import kotlin.test.Test

/**
 *
 * Created on 18/01/2018.
 *
 * @author PierreJean
 */
class MemoryStorageTest {

    @Test
    fun `Load provides constructor data right after construction`() {
        val sut = MemoryStorage("hello!")

        assertThat(sut.load(), equalTo("hello!"))
    }

    @Test
    fun `Load provides saved data `() {
        val sut = MemoryStorage("hello!")

        sut.save("sip")

        assertThat(sut.load(), equalTo("sip"))
    }
}