package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import static java.util.Collections.emptyList;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public abstract class TasksStateTextUtil {

    public static ViewState defaultState() {
        return ViewState.builder()
                .showSuccessfullySavedMessage(false)
                .showAddTask(false)
                .showTaskDetails(null)
                .showCompletedTasksCleared(false)
                .showTaskMarkedActive(false)
                .showTaskMarkedComplete(false)
                .tasks(emptyList())
                .clearCompletedInProgress(false)
                .activateTaskInProgress(false)
                .completeTaskInProgress(false)
                .showTaskMarkedActiveFailed(null)
                .showLoadingTasksError(false)
                .loadingIndicator(false)
                .build();
    }

}