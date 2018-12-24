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
import com.pij.utils.Logger;
import com.pij.utils.SysoutLogger;

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
                .stateFilter((left, right) -> false)
                ;
    }

    public abstract Logger logger();

    abstract Collection<ReducerCreator<?, S>> creators();

    abstract StateConverter<S, M> stateToModel();

    /**
     * Resets those properties of the state deemed to be transient, i.e. those that are events
     * the view should act upon only once.
     * The default leaves the state unchanged.
     */
    abstract TransientCleaner<S> transientResetter();

    /**
     * An Engine can use this to determine if a state has changed enough to be emitted.
     * This is useful when a UI blindly re-emits the same values and trigger the same feature.
     * An optimal configuration is:<ul>
     * <li>the configuration's state filter checks for sameness</li>
     * <li>Individual feature emit the same state if their input hasn't caused a real change
     * in state</li>
     * </ul>
     * An alternative is to check for equality, rather than sameness:<ul>
     * <li>There is no need for intelligent features</li>
     * <li>There is a performance penalty as <pre>State.equals()</pre> is called for every state</li>
     * </ul>
     * The default state filter considers all states to be different.
     */
    abstract StateEquality<S> stateFilter();

    abstract Storage<S> store();

    @AutoValue.Builder
    public abstract static class Builder<S, M> {

        public abstract Builder<S, M> logger(Logger logger);

        public abstract Builder<S, M> creators(Collection<ReducerCreator<?, S>> creators);

        public abstract Builder<S, M> stateToModel(StateConverter<S, M> stateToModel);

        public abstract Builder<S, M> transientResetter(TransientCleaner<S> transientResetter);

        public abstract Builder<S, M> stateFilter(StateEquality<S> stateFilter);

        public abstract Builder<S, M> store(Storage<S> storage);

        public abstract Configuration<S, M> build();
    }
}
