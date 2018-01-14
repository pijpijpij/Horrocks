package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import com.pij.horrocks.Result;

import io.reactivex.functions.Function;

/**
 * <p>Created on 05/01/2018.</p>
 *
 * @author PierreJean
 */
class ShowAddTaskFeature implements Function<Object, Result<ViewState>> {

    @Override
    public Result<ViewState> apply(Object event) throws Exception {
        return current -> current.toBuilder().showAddTask(true).build();
    }
}
