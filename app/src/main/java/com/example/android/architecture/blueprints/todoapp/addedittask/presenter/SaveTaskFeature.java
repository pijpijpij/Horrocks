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
import com.pij.horrocks.Logger;
import com.pij.horrocks.Result;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * @author PierreJean
 */
class SaveTaskFeature implements Function<Task, Observable<Result<AddEditTaskModel>>> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    SaveTaskFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static AddEditTaskModel updateInvalidState(AddEditTaskModel current) {
        return current.toBuilder()
                .showEmptyTaskError(true)
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
    private static AddEditTaskModel updateSuccessState(AddEditTaskModel current) {
        return current.toBuilder()
                // After an edit, go back to the list.
                .showTasksList(true)
//                .loadingIndicator(false)
                .build();
    }

    @NonNull
    private static AddEditTaskModel updateFailureState(AddEditTaskModel current) {
        return current.toBuilder()
//                .loadingIndicator(false)
                .build();
    }

    @Override
    public Observable<Result<AddEditTaskModel>> apply(Task toSave) {
        return Observable.just(toSave)
                .filter(Task::isEmpty)
                .map(task -> (Result<AddEditTaskModel>) SaveTaskFeature::updateInvalidState)
                .switchIfEmpty(Util.saveTaskAsCompletable(toSave, dataSource)
                        .doOnError(e -> logger.print(getClass(), "Could not save data", e))
                        .andThen(Observable.just((Result<AddEditTaskModel>) SaveTaskFeature::updateSuccessState))
                        .onErrorReturnItem(SaveTaskFeature::updateFailureState)
                        .startWith(SaveTaskFeature::updateStartState));
    }

}
