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

package com.example.android.architecture.blueprints.todoapp.addedittask.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskModel;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.Util;
import com.google.common.base.Strings;
import com.pij.horrocks.Logger;
import com.pij.horrocks.Result;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */
class LoadTaskFeature implements Function<String, Observable<Result<AddEditTaskModel>>> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    LoadTaskFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static AddEditTaskModel updateNewTaskState(AddEditTaskModel current) {
        return current.toBuilder()
                .showTitle("")
                .showDescription("")
//                .loadingIndicator(false)
                .build();
    }

    @NonNull
    private static AddEditTaskModel updateStartState(AddEditTaskModel current) {
        return current.toBuilder()
//                .loadingIndicator(true)
                .build();
    }

    @NonNull
    private static AddEditTaskModel updateSuccessState(AddEditTaskModel current, Task response) {
        return current.toBuilder()
                .showTitle(response.getTitle())
                .showDescription(response.getDescription())
//                .loadingIndicator(false)
                .build();
    }

    @NonNull
    private static AddEditTaskModel updateFailureState(AddEditTaskModel current) {
        return current.toBuilder()
                .showEmptyTaskError(true)
//                .loadingIndicator(false)
                .build();
    }

    @Override
    public Observable<Result<AddEditTaskModel>> apply(String taskId) {
        return Observable.just(taskId)
                .filter(Strings::isNullOrEmpty)
                .map(id -> (Result<AddEditTaskModel>) LoadTaskFeature::updateNewTaskState)
                .switchIfEmpty(
                        Util.loadTaskAsSingle(taskId, dataSource)
                                .doOnError(e -> logger.print(getClass(), "Could not load data", e))
                                .map(task -> (Result<AddEditTaskModel>) current -> updateSuccessState(current, task))
                                .onErrorReturnItem(LoadTaskFeature::updateFailureState)
                                .toObservable()
                                .startWith(LoadTaskFeature::updateStartState)
                )
                ;
    }

}
