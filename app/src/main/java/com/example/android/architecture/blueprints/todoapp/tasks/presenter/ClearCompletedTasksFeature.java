package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.Util;
import com.pij.horrocks.Logger;
import com.pij.horrocks.Result;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * <p>Created on 04/01/2018.</p>
 *
 * @author PierreJean
 */
class ClearCompletedTasksFeature implements Function<Object, Observable<Result<ViewState>>> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    ClearCompletedTasksFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static ViewState updateStartState(ViewState current) {
        return current.toBuilder()
                .clearCompletedInProgress(true)
                .build();
    }

    @NonNull
    private static ViewState updateSuccessState(ViewState current, List<Task> list) {
        return current.toBuilder()
                .showCompletedTasksCleared(true)
                .clearCompletedInProgress(false)
                .tasks(list)
                .build();
    }

    @NonNull
    private static ViewState updateFailureState(ViewState current, @SuppressWarnings("unused") Throwable error) {
        return current.toBuilder()
                // TODO add an error condition.
                .clearCompletedInProgress(false)
                .build();
    }

    /**
     * Ignores errors from the repository.
     */
    @NonNull
    @Override
    public Observable<Result<ViewState>> apply(Object event) {
        return Completable.fromAction(dataSource::clearCompletedTasks)
                .andThen(Util.loadTasksAsSingle(dataSource))
                .map(list -> (Result<ViewState>) current -> updateSuccessState(current, list))
                .doOnError(e -> logger.print(getClass(), "Could no clear completed tasks", e))
                .onErrorReturn(e -> current -> updateFailureState(current, e))
                .toObservable()
                .startWith(ClearCompletedTasksFeature::updateStartState);
    }

}
