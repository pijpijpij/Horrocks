package com.example.android.architecture.blueprints.todoapp.taskdetail.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.Util;
import com.example.android.architecture.blueprints.todoapp.taskdetail.ViewModel;
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
class LoadTaskFeature implements Function<String, Observable<Result<ViewModel>>> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    LoadTaskFeature(Logger logger, TasksDataSource dataSource) {
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
    private static ViewModel updateSuccessState(ViewModel current, Task response) {
        return current.toBuilder()
                .showTitle(response.getTitle())
                .showDescription(response.getDescription())
                .showCompletionStatus(response.isCompleted())
                .loadingIndicator(false)
                .build();
    }

    @NonNull
    private static ViewModel updateFailureState(ViewModel current) {
        return current.toBuilder()
                .showMissingTask(true)
                .loadingIndicator(false)
                .build();
    }

    @Override
    public Observable<Result<ViewModel>> apply(String taskId) {
        return Observable.just(taskId)
                .filter(Strings::isNullOrEmpty)
                .map(id -> (Result<ViewModel>) LoadTaskFeature::updateInvalidState)
                .switchIfEmpty(
                        Util.loadTaskAsSingle(taskId, dataSource)
                                .doOnError(e -> logger.print(getClass(), "Could no load data", e))
                                .map(task -> (Result<ViewModel>) current -> updateSuccessState(current, task))
                                .onErrorReturnItem(LoadTaskFeature::updateFailureState)
                                .toObservable()
                                .startWith(LoadTaskFeature::updateStartState)
                )
                ;
    }

}
