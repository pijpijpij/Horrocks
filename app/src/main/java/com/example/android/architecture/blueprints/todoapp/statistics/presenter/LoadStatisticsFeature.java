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

package com.example.android.architecture.blueprints.todoapp.statistics.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.Util;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsModel;
import com.pij.horrocks.Logger;
import com.pij.horrocks.Result;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */
class LoadStatisticsFeature implements Function<Object, Observable<Result<StatisticsModel>>> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    LoadStatisticsFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static StatisticsModel updateStartState(StatisticsModel current) {
        return current.toBuilder()
                .progressIndicator(true)
                .build();
    }

    @NonNull
    private static StatisticsModel updateSuccessState(StatisticsModel current, StatisticsModel.Numbers statistics) {
        return current.toBuilder()
                .progressIndicator(false)
                .showStatistics(statistics)
                .build();
    }

    @NonNull
    private static StatisticsModel updateFailureState(StatisticsModel current, Throwable error) {
        return current.toBuilder()
                .showLoadingStatisticsError(true)
                .progressIndicator(false)
                .build();
    }

    @Override
    public Observable<Result<StatisticsModel>> apply(Object event) {
        return Util.loadTasksAsSingle(dataSource)
                .doOnError(e -> logger.print(getClass(), "Could not load data", e))
                .flatMapObservable(Observable::fromIterable)
                .publish(task -> Single.zip(
                        task.filter(Task::isActive).count().map(Long::intValue),
                        task.filter(Task::isCompleted).count().map(Long::intValue),
                        StatisticsModel.Numbers::create
                ).toObservable())
                .first(StatisticsModel.Numbers.create(0, 0))
                .map(numbers -> (Result<StatisticsModel>) current -> updateSuccessState(current, numbers))
                .onErrorReturn(e -> current -> updateFailureState(current, e))
                .toObservable()
                .startWith(LoadStatisticsFeature::updateStartState);
    }

}
