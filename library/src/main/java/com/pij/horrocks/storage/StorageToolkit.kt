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

import java.io.InputStream
import java.io.OutputStream

/**
 * <p>Created on 28/09/2018.</p>
 * @author PierreJean
 */
class StorageToolkit<S>(private val serializer: Serializer<S>,
                        private val deserializer: Deserializer<S>,
                        private val streamAccessor: StreamAccessor) {

    fun openForRead() = streamAccessor.openForRead()
    fun openForAppend() = streamAccessor.openForAppend()
    fun close(stream: InputStream) = streamAccessor.close(stream)
    fun close(stream: OutputStream) = streamAccessor.close(stream)

    fun readLast(input: InputStream): S = deserializer.invoke(input)
    fun write(data: S, output: OutputStream) = serializer.invoke(data, output)
}

typealias Deserializer<S> = (InputStream) -> S
typealias Serializer<S> = (S, OutputStream) -> Unit