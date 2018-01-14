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

    public abstract S initialState();

    public abstract Function<S, M> stateToModel();

    public abstract Function<S, S> transientResetter();

    @AutoValue.Builder
    public abstract static class Builder<S, M> {
        public abstract Builder<S, M> logger(Logger logger);

        public abstract Builder<S, M> features(Collection<Feature<?, S>> features);

        public abstract Builder<S, M> initialState(S initialState);

        public abstract Builder<S, M> stateToModel(Function<S, M> stateToModel);

        public abstract Builder<S, M> transientResetter(Function<S, S> transientResetter);

        public abstract Configuration<S, M> build();
    }
}
