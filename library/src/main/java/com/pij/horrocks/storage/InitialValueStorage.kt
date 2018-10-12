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

import java.util.concurrent.Callable

/**
 * <p>Created on 28/09/2018.</p>
 * @author PierreJean
 */
class InitialValueStorage<S>(private val decorated: Storage<S>, private val defaultInitialValue: Callable<S>) : Storage<S> {

    private val passThrough = { decorated.load() }

    /** The initial value of load().
     */
    private var loadIt: () -> S = {
        try {
            loadIt = passThrough
            passThrough()
        } catch (e: Exception) {
            defaultInitialValue.call()
        }
    }

    override fun load(): S = loadIt()

    override fun save(state: S) = decorated.save(state)
}

/** Convenience extension method.
 */
fun <S> Storage<S>.initialValue(initialValue: Callable<S>): Storage<S> {
    return InitialValueStorage(this, initialValue)
}
