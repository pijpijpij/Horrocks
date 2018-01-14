package com.pij.horrocks;

import java.util.Collection;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * <p>Created on 14/12/2017.</p>
 *
 * @author PierreJean
 */

public final class DefaultEngine<S, M> implements Engine<S, M> {

    private final Logger logger;

    public DefaultEngine(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Observable<M> runWith(Configuration<S, M> configuration) {
        S initialState = configuration.initialState();
        Collection<Feature<?, S>> features = configuration.features();
        Function<S, S> transientCleaner = configuration.transientResetter();
        Function<S, M> stateConverter = configuration.stateToModel();
        return Observable.fromIterable(features)
                .flatMap(Feature::result)
                .doOnNext(this::logResult)
                .scan(initialState, (current, result) -> updateState(current, result, transientCleaner))
                .doOnNext(this::logState)
                .map(stateConverter)
                .doOnNext(this::logModel)
                .doOnError(e -> logger.print(getClass(), "Terminal failure!!!", e))
                ;
    }

    private void logModel(M it) {
        logger.print(getClass(), "Emitting " + it);
    }

    private void logState(S it) {
        logger.print(getClass(), "Calculating " + it);
    }

    private void logResult(Result it) {
        logger.print(getClass(), "Received " + it);
    }

    private S updateState(S current, Result<S> result, Function<S, S> transientCleaner) throws Exception {
        S transientCleaned = transientCleaner.apply(current);
        return result.applyTo(transientCleaned);
    }

}
