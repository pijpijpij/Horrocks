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

    abstract Collection<ActionCreator<?, S>> creators();

    abstract StateConverter<S, M> stateToModel();

    abstract TransientCleaner<S> transientResetter();

    abstract Storage<S> store();

    @AutoValue.Builder
    public abstract static class Builder<S, M> {

        public abstract Builder<S, M> logger(Logger logger);

        public abstract Builder<S, M> creators(Collection<ActionCreator<?, S>> creators);

        public abstract Builder<S, M> stateToModel(StateConverter<S, M> stateToModel);

        public abstract Builder<S, M> transientResetter(TransientCleaner<S> transientResetter);

        public abstract Builder<S, M> store(Storage<S> storage);

        public abstract Configuration<S, M> build();
    }
}
