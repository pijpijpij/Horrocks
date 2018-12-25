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

import com.pij.utils.Logger;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */

public final class SingleReducerCreator<E, S> implements TriggeredReducerCreator<E, S> {
    private final Subject<E> event = PublishSubject.create();
    private final Interaction<E, S> interaction;
    private final Logger logger;

    public SingleReducerCreator(@NonNull Interaction<E, S> interaction) {
        this(interaction, Logger.NOOP);
    }

    public SingleReducerCreator(@NonNull Interaction<E, S> interaction, @NonNull Logger logger) {
        this.interaction = interaction;
        this.logger = logger;
    }

    @Override
    public void trigger(@NonNull E event) {
        logReceivedEvent(event);
        this.event.onNext(event);
    }

    @NonNull
    @Override
    public Observable<Reducer<S>> reducers() {
        return event
                .doOnNext(this::logProcessingEvent)
                .map(interaction::process)
                .doOnNext(this::logReducer)
                ;
    }

    private void logReceivedEvent(@NonNull E event) {
        logger.print(interaction.getClass(), "Received event %s", event);
    }

    private void logProcessingEvent(E event) {
        logger.print(interaction.getClass(), "Processing event %s", event);
    }

    private void logReducer(Reducer<S> reducer) {
        logger.print(interaction.getClass(), "Emitting reducer %s", reducer);
    }

}
