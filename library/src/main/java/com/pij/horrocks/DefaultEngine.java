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

import com.pij.utils.Logger;

import java.util.Collection;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

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
        Storage<S> storage = configuration.store();
        Collection<ReducerCreator<S>> reducerCreators = configuration.creators();
        TransientCleaner<S> transientCleaner = configuration.transientResetter();
        StateEquality<S> stateFilter = configuration.stateFilter();
        StateConverter<S, M> stateConverter = configuration.stateToModel();
        ErrorReducerFactory<S> errorReducerFactory = configuration.errorReducerFactory();
        Callable<S> initialValue = () -> transientCleaner.clean(storage.load());
        return Observable.fromIterable(reducerCreators)
                .flatMap(feature -> feature.reducers()
                        .doOnTerminate(() -> logger.print(getClass(), "ReducerCreator %s Unexpected completion!!!", feature.hashCode()))
                        .onErrorReturn(errorReducerFactory::create)
                        .retry()
                )
                .scanWith(initialValue, (current, reducer) -> updateState(current, reducer, transientCleaner))
                .distinctUntilChanged(stateFilter::equal)
                .doOnNext(this::logState)
                .doOnNext(storage::save)
                .map(stateConverter::convert)
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

    private S updateState(S current, Reducer<S> reducer, TransientCleaner<S> transientCleaner) {
        S transientCleaned = transientCleaner.clean(current);
        return reducer.reduce(transientCleaned);
    }

}
