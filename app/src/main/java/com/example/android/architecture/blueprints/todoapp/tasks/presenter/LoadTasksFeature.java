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
import com.example.android.architecture.blueprints.todoapp.tasks.FilterType;
import com.pij.horrocks.AsyncInteraction;
import com.pij.horrocks.Logger;
import com.pij.horrocks.Reducer;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Predicate;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */
class LoadTasksFeature implements AsyncInteraction<FilterType, ViewState> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    LoadTasksFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static ViewState updateStartState(ViewState current) {
        return current.toBuilder()
                .loadingIndicator(true)
                .build();
    }

    @NonNull
    private static ViewState updateSuccessState(ViewState current, List<Task> list) {
        return current.toBuilder()
                .tasks(list)
                .loadingIndicator(false)
                .build();
    }

    @NonNull
    private static ViewState updateFailureState(ViewState current, Throwable error) {
        return current.toBuilder()
                .showLoadingTasksError(true)
                .loadingIndicator(false)
                .build();
    }

    @NonNull
    @Override
    public Observable<Reducer<ViewState>> process(@NonNull FilterType filter) {
        return Util.loadTasksAsSingle(dataSource)
                .doOnError(e -> logger.print(getClass(), "Could not load data", e))
                .flatMap(list -> Observable.fromIterable(list).filter(filterFor(filter)).toList())
                .doOnError(e -> logger.print(getClass(), "Could not filter data", e))
                .map(list -> (Reducer<ViewState>) current -> updateSuccessState(current, list))
                .onErrorReturn(e -> current -> updateFailureState(current, e))
                .toObservable()
                .startWith(LoadTasksFeature::updateStartState);
    }

    /**
     * TODO improve filtering performance, if needed (for ALL_TASKS)
     */
    private Predicate<Task> filterFor(FilterType filter) {
        switch (filter) {
            case ACTIVE_TASKS:
                return Task::isActive;
            case COMPLETED_TASKS:
                return Task::isCompleted;
            case ALL_TASKS:
                return ignored -> true;
            default:
                throw new IllegalArgumentException("Filter unsupported: " + filter);
        }
    }

}
