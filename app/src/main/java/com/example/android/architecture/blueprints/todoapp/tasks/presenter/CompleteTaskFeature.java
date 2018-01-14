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
class CompleteTaskFeature implements Function<Task, Observable<Result<ViewState>>> {

    private final Logger logger;
    private final TasksDataSource dataSource;

    CompleteTaskFeature(Logger logger, TasksDataSource dataSource) {
        this.logger = logger;
        this.dataSource = dataSource;
    }

    @NonNull
    private static ViewState updateStartState(ViewState current) {
        return current.toBuilder()
                .completeTaskInProgress(true)
                .build();
    }

    @NonNull
    private static ViewState updateSuccessState(ViewState current, List<Task> list) {
        return current.toBuilder()
                .showTaskMarkedComplete(true)
                .completeTaskInProgress(false)
                .tasks(list)
                .build();
    }

    @NonNull
    private static ViewState updateFailureState(ViewState current, @SuppressWarnings("unused") Throwable error) {
        return current.toBuilder()
                // TODO add an error condition.
                .completeTaskInProgress(false)
                .build();
    }

    /**
     * Ignores errors from the repository.
     */
    @NonNull
    @Override
    public Observable<Result<ViewState>> apply(Task event) {
        return Completable.fromAction(() -> dataSource.completeTask(event))
                .andThen(Util.loadTasksAsSingle(dataSource))
                .map(list -> (Result<ViewState>) current -> updateSuccessState(current, list))
                .doOnError(e -> logger.print(getClass(), "Could no complete task " + event, e))
                .onErrorReturn(e -> current -> updateFailureState(current, e))
                .toObservable()
                .startWith(CompleteTaskFeature::updateStartState);
    }

}
