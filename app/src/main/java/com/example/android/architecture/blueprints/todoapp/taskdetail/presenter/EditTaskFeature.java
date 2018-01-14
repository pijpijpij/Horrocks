package com.example.android.architecture.blueprints.todoapp.taskdetail.presenter;

import com.example.android.architecture.blueprints.todoapp.taskdetail.ViewModel;
import com.google.common.base.Strings;
import com.pij.horrocks.Result;

import io.reactivex.functions.Function;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */
class EditTaskFeature implements Function<String, Result<ViewModel>> {

    @Override
    public Result<ViewModel> apply(String taskId) {
        String actualId = Strings.isNullOrEmpty(taskId) ? null : taskId;
        return current -> current.toBuilder().showEditTask(actualId).build();
    }

}
