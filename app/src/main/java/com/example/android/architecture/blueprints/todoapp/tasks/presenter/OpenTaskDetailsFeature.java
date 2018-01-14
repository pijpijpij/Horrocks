package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.pij.horrocks.Result;

import io.reactivex.functions.Function;

/**
 * <p>Created on 04/01/2018.</p>
 *
 * @author PierreJean
 */
class OpenTaskDetailsFeature implements Function<Task, Result<ViewState>> {

    @Override
    public Result<ViewState> apply(Task event) throws Exception {
        return current -> current.toBuilder().showTaskDetails(event.getId()).build();
    }
}
