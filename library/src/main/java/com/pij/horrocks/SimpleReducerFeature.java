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

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */

public final class SimpleReducerFeature<E, S> implements Feature<E, S> {
    private final Subject<E> event = PublishSubject.create();
    private final Reducer<E, S> reducer;
    private final Logger logger;

    public SimpleReducerFeature(@NonNull Reducer<E, S> reducer) {
        this(reducer, Logger.NOOP);
    }

    public SimpleReducerFeature(@NonNull Reducer<E, S> reducer, @NonNull Logger logger) {
        this.reducer = reducer;
        this.logger = logger;
    }

    @Override
    public void trigger(@NonNull E event) {
        logReceivedEvent(event);
        this.event.onNext(event);
    }

    @NonNull
    @Override
    public Observable<? extends ResultReducer<S>> results() {
        return event
                .doOnNext(event -> logProcessingEvent(event, logger))
                .map(event -> (ResultReducer<S>) current -> reducer.reduce(event, current))
                .doOnNext(result -> logResult(result, logger))
                ;
    }

    private void logReceivedEvent(@NonNull E event) {
        logger.print(reducer.getClass(), "Received event " + event);
    }

    private void logProcessingEvent(E event, Logger logger) {
        logger.print(reducer.getClass(), "Processing event " + event);
    }

    private void logResult(ResultReducer<S> result, Logger logger) {
        logger.print(reducer.getClass(), "Emitting result " + result);
    }

}
