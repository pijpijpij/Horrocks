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

import static com.example.android.architecture.blueprints.todoapp.data.source.Util.errorMessage;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */
class ActivateTaskFeature implements Function<Task, Observable<Result<ViewState>>> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    ActivateTaskFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static ViewState updateStartState(ViewState current) {
        return current.toBuilder()
                .activateTaskInProgress(true)
                .build();
    }

    @NonNull
    private static ViewState updateSuccessState(ViewState current, List<Task> list) {
        return current.toBuilder()
                .showTaskMarkedActive(true)
                .tasks(list)
                .activateTaskInProgress(false)
                .build();
    }

    @NonNull
    private static ViewState updateFailureState(ViewState current, Throwable error) {
        return current.toBuilder()
                .showTaskMarkedActiveFailed(errorMessage(error))
                .activateTaskInProgress(false)
                .build();
    }

    @Override
    public Observable<Result<ViewState>> apply(Task event) throws Exception {
        return Completable.fromAction(() -> dataSource.activateTask(event))
                .doOnError(e -> logger.print(getClass(), "Could no activate task " + event, e))
                .andThen(Util.loadTasksAsSingle(dataSource)
                        .doOnError(e -> logger.print(getClass(), "Could no load tasks " + event, e))
                        .map(list -> (Result<ViewState>) current -> updateSuccessState(current, list))
                )
                .onErrorReturn(e -> current -> updateFailureState(current, e))
                .toObservable()
                .startWith(ActivateTaskFeature::updateStartState);
    }

}
