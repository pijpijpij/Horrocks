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

package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.Util;
import com.pij.horrocks.AsyncInteraction;
import com.pij.horrocks.Reducer;
import com.pij.utils.Logger;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

import static com.example.android.architecture.blueprints.todoapp.data.source.Util.errorMessage;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */
class ActivateTaskFeature implements AsyncInteraction<Task, ViewState> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    ActivateTaskFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static ViewState updateStartState(ViewState current) {
        return current.toBuilder()
                .activateTaskInProgress(true)
                .build();
    }

    @NonNull
    private static ViewState updateSuccessState(ViewState current, List<Task> list) {
        return current.toBuilder()
                .showTaskMarkedActive(true)
                .tasks(list)
                .activateTaskInProgress(false)
                .build();
    }

    @NonNull
    private static ViewState updateFailureState(ViewState current, Throwable error) {
        return current.toBuilder()
                .showTaskMarkedActiveFailed(errorMessage(error))
                .activateTaskInProgress(false)
                .build();
    }

    @NonNull
    @Override
    public Observable<Reducer<ViewState>> process(@NonNull Task event) {
        return Completable.fromAction(() -> dataSource.activateTask(event))
                .doOnError(e -> logger.print(getClass(), e, "Could not activate task %s", event))
                .andThen(Util.loadTasksAsSingle(dataSource)
                        .doOnError(e -> logger.print(getClass(), e, "Could not load tasks %s", event))
                        .map(list -> (Reducer<ViewState>) current -> updateSuccessState(current, list))
                )
                .onErrorReturn(e -> current -> updateFailureState(current, e))
                .toObservable()
                .startWith(ActivateTaskFeature::updateStartState);
    }

}
