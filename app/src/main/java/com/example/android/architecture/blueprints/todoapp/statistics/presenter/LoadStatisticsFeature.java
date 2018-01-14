package com.example.android.architecture.blueprints.todoapp.statistics.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.Util;
import com.example.android.architecture.blueprints.todoapp.statistics.ViewModel;
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
class LoadStatisticsFeature implements Function<Object, Observable<Result<ViewModel>>> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    LoadStatisticsFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static ViewModel updateStartState(ViewModel current) {
        return current.toBuilder()
                .progressIndicator(true)
                .build();
    }

    @NonNull
    private static ViewModel updateSuccessState(ViewModel current, ViewModel.Numbers statistics) {
        return current.toBuilder()
                .progressIndicator(false)
                .showStatistics(statistics)
                .build();
    }

    @NonNull
    private static ViewModel updateFailureState(ViewModel current, Throwable error) {
        return current.toBuilder()
                .showLoadingStatisticsError(true)
                .progressIndicator(false)
                .build();
    }

    @Override
    public Observable<Result<ViewModel>> apply(Object event) {
        return Util.loadTasksAsSingle(dataSource)
                .doOnError(e -> logger.print(getClass(), "Could no load data", e))
                .flatMapObservable(Observable::fromIterable)
                .publish(task -> Single.zip(
                        task.filter(Task::isActive).count().map(Long::intValue),
                        task.filter(Task::isCompleted).count().map(Long::intValue),
                        ViewModel.Numbers::create
                ).toObservable())
                .first(ViewModel.Numbers.create(0, 0))
                .map(numbers -> (Result<ViewModel>) current -> updateSuccessState(current, numbers))
                .onErrorReturn(e -> current -> updateFailureState(current, e))
                .toObservable()
                .startWith(LoadStatisticsFeature::updateStartState);
    }

}
