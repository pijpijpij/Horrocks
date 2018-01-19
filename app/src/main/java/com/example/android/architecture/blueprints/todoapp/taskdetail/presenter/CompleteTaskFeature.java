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
import com.example.android.architecture.blueprints.todoapp.taskdetail.ViewModel;
import com.google.common.base.Strings;
import com.pij.horrocks.Logger;
import com.pij.horrocks.Result;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */
class CompleteTaskFeature implements Function<String, Observable<Result<ViewModel>>> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    CompleteTaskFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static ViewModel updateInvalidState(ViewModel current) {
        return current.toBuilder()
                .showMissingTask(true)
                .loadingIndicator(false)
                .build();
    }

    @NonNull
    private static ViewModel updateStartState(ViewModel current) {
        return current.toBuilder()
                .loadingIndicator(true)
                .build();
    }

    @NonNull
    private static ViewModel updateSuccessState(ViewModel current) {
        return current.toBuilder()
                .completed(true)
                .showTaskMarkedComplete(true)
                .loadingIndicator(false)
                .build();
    }

    @NonNull
    private static ViewModel updateFailureState(ViewModel current) {
        return current.toBuilder()
                .loadingIndicator(false)
                .build();
    }

    @Override
    public Observable<Result<ViewModel>> apply(String taskId) {
        return Observable.just(taskId)
                .filter(Strings::isNullOrEmpty)
                .map(id -> (Result<ViewModel>) CompleteTaskFeature::updateInvalidState)
                .switchIfEmpty(
                        Completable.fromAction(() -> dataSource.completeTask(taskId))
                                .doOnError(e -> logger.print(getClass(), "Could no update data", e))
                                .andThen(Observable.just((Result<ViewModel>) CompleteTaskFeature::updateSuccessState))
                                .onErrorReturnItem(CompleteTaskFeature::updateFailureState)
                                .startWith(CompleteTaskFeature::updateStartState)
                );
    }

}
