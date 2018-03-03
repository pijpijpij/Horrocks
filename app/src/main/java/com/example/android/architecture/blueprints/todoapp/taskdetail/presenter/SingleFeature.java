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

package com.example.android.architecture.blueprints.todoapp.taskdetail.presenter;

import com.pij.horrocks.Reducer;
import com.pij.horrocks.ResultReducer;

import io.reactivex.functions.Function;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */
class SingleFeature<E, S> implements Function<E, ResultReducer<S>> {

    private final Reducer<E, S> reducer;

    public SingleFeature(Reducer<E, S> reducer) {
        this.reducer = reducer;
    }

    @Override
    public final ResultReducer<S> apply(E event) {
        return current -> reducer.reduce(event, current);
    }

}
