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

package com.example.android.architecture.blueprints.todoapp.taskdetail.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailModel;
import com.google.common.base.Strings;
import com.pij.horrocks.AsyncInteraction;
import com.pij.horrocks.Reducer;
import com.pij.utils.Logger;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */
class DeleteTaskFeature implements AsyncInteraction<String, TaskDetailModel> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    DeleteTaskFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static TaskDetailModel updateInvalidState(TaskDetailModel current) {
        return current.toBuilder()
                .showMissingTask(true)
                .loadingIndicator(false)
                .build();
    }

    @NonNull
    private static TaskDetailModel updateStartState(TaskDetailModel current) {
        return current.toBuilder()
                .loadingIndicator(true)
                .build();
    }

    @NonNull
    private static TaskDetailModel updateSuccessState(TaskDetailModel current) {
        return current.toBuilder()
                .close(true)
                .loadingIndicator(false)
                .build();
    }

    @NonNull
    private static TaskDetailModel updateFailureState(TaskDetailModel current) {
        return current.toBuilder()
                .loadingIndicator(false)
                .build();
    }

    @NonNull
    @Override
    public Observable<Reducer<TaskDetailModel>> process(@NonNull String taskId) {
        return Observable.just(taskId)
                .filter(Strings::isNullOrEmpty)
                .map(id -> (Reducer<TaskDetailModel>) DeleteTaskFeature::updateInvalidState)
                .switchIfEmpty(
                        Completable.fromAction(() -> dataSource.deleteTask(taskId))
                                .doOnError(e -> logger.print(getClass(), "Could not delete data", e))
                                .andThen(Observable.just((Reducer<TaskDetailModel>) DeleteTaskFeature::updateSuccessState))
                                .onErrorReturnItem(DeleteTaskFeature::updateFailureState)
                                .startWith(DeleteTaskFeature::updateStartState)
                );
    }

}
