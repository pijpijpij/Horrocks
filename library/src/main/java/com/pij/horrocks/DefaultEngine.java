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

import java.util.Collection;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
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
        Store<S> store = configuration.store();
        Collection<Feature<?, S>> features = configuration.features();
        Function<S, S> transientCleaner = configuration.transientResetter();
        Function<S, M> stateConverter = configuration.stateToModel();
        Callable<S> initialValue = () -> transientCleaner.apply(store.load());
        return Observable.fromIterable(features)
                .flatMap(feature -> feature.result()
                        .doOnTerminate(() -> logger.print(getClass(), "Feature %s Unexpected completion!!!", feature.hashCode()))
                )
                .scanWith(initialValue, (current, result) -> updateState(current, result, transientCleaner))
                .doOnNext(this::logState)
                .doOnNext(store::save)
                .map(stateConverter)
                .doOnNext(this::logModel)
                .doOnError(this::logTerminalFailure)
                .doOnComplete(this::logUnexpectedCompletion)
                .doOnDispose(this::logDispose)
                .doOnSubscribe(this::logSubscribe)
                ;
    }

    private void logSubscribe(@SuppressWarnings("unused") Disposable ignored) {
        logger.print(getClass(), "Engine " + hashCode() + " Start of this run");
    }

    private void logDispose() {
        logger.print(getClass(), "Engine %d End of this run", hashCode());
    }

    private void logUnexpectedCompletion() {
        logger.print(getClass(), "Engine %d Unexpected completion!!!", hashCode());
    }

    private void logTerminalFailure(Throwable e) {
        logger.print(getClass(), e, "Engine %s Terminal failure!!! ", hashCode());
    }

    private void logModel(M it) {
        logger.print(getClass(), "Emitting %s", it);
    }

    private void logState(S it) {
        logger.print(getClass(), "Calculating %s", it);
    }

    private S updateState(S current, Result<S> result, Function<S, S> transientCleaner) throws Exception {
        S transientCleaned = transientCleaner.apply(current);
        return result.applyTo(transientCleaned);
    }

}
