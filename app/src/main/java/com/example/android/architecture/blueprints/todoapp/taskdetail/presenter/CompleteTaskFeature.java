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
                                .doOnError(e -> logger.print(getClass(), "Could no delete data", e))
                                .andThen(Observable.just((Result<ViewModel>) CompleteTaskFeature::updateSuccessState))
                                .onErrorReturnItem(CompleteTaskFeature::updateFailureState)
                                .startWith(CompleteTaskFeature::updateStartState)
                );
    }

}
