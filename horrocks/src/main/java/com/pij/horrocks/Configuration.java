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

import com.google.auto.value.AutoValue;

import java.util.Collection;

import io.reactivex.functions.Function;

/**
 * <p>Created on 14/12/2017.</p>
 *
 * @author PierreJean
 */
@AutoValue
public abstract class Configuration<S, M> {

    public static <S, M> Builder<S, M> builder() {
        AutoValue_Configuration.Builder<S, M> result = new AutoValue_Configuration.Builder<>();
        return result
                .logger(new SysoutLogger())
                .transientResetter(s -> s)
                ;
    }

    public abstract Logger logger();

    public abstract Collection<Feature<?, S>> features();

    public abstract Function<S, M> stateToModel();

    public abstract Function<S, S> transientResetter();

    public abstract Store<S> store();

    @AutoValue.Builder
    public abstract static class Builder<S, M> {

        public abstract Builder<S, M> logger(Logger logger);

        public abstract Builder<S, M> features(Collection<Feature<?, S>> features);

        public abstract Builder<S, M> stateToModel(Function<S, M> stateToModel);

        public abstract Builder<S, M> transientResetter(Function<S, S> transientResetter);

        public abstract Builder<S, M> store(Store<S> store);

        public abstract Configuration<S, M> build();
    }
}
