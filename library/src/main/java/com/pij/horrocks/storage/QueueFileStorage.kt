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

import com.squareup.tape.FileObjectQueue
import java.io.ByteArrayInputStream
import java.io.File
import java.io.OutputStream

/**
 * <p>Created on 28/09/2018.</p>
 * @author PierreJean
 */
class QueueFileStorage<S>(file: File,
                          serializer: Serializer<S>,
                          deserializer: Deserializer<S>) : Storage<S> {

    private val implementation: FileObjectQueue<S> by lazy {
        FileObjectQueue<S>(file, object : FileObjectQueue.Converter<S> {
            override fun from(input: ByteArray): S = deserializer.invoke(ByteArrayInputStream(input))
            override fun toStream(input: S, output: OutputStream) = serializer.invoke(input, output)
        })
    }

    override fun load(): S = implementation.peek()

    override fun save(state: S) = implementation.add(state)

}