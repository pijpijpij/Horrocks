package com.example.android.architecture.blueprints.todoapp.taskdetail.presenter;

import com.example.android.architecture.blueprints.todoapp.taskdetail.ViewModel;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public abstract class TaskDetailModelTextUtil {

    public static ViewModel defaultState() {
        return ViewModel.builder()
                .loadingIndicator(false)
                .close(false)
                .showCompletionStatus(false)
                .showTitle(null)
                .showDescription(null)
                .showEditTask(null)
                .showMissingTask(false)
                .showTaskMarkedActive(false)
                .showTaskMarkedComplete(false)
                .build();
    }

}