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

package com.pij.horrocks;

import android.support.annotation.NonNull;

/**
 * An instance of {@link ResultReducer} has the ability to apply results of an interaction to a state. It is very similar to a
 * <code>Reducer</code> in Redux. The difference is that it can - and should - include the result of an interaction with
 * <p>Created on 01/01/2018.</p>
 *
 * @param <S> state this results can alter.
 * @author PierreJean
 */

public interface ResultReducer<S> {

    /**
     * <h3>Note</h3>This method used to be <code>applyTo(S)</code> in earlier versions.
     *
     * @param current current state
     * @return new state after the results this object contains have been applied to the <code>current</code> state.
     */
    @NonNull
    S applyTo(@NonNull S current);
}
