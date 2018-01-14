package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.google.auto.value.AutoValue;

import java.util.List;

/**
 * <p>Created on 14/12/2017.</p>
 *
 * @author PierreJean
 */

@AutoValue
abstract class ViewState {

    public static Builder builder() {
        return new AutoValue_ViewState.Builder();
    }

    public abstract boolean showSuccessfullySavedMessage();

    public abstract boolean showAddTask();

    @Nullable
    public abstract String showTaskDetails();

    public abstract boolean showCompletedTasksCleared();

    public abstract boolean showTaskMarkedActive();

    public abstract boolean showTaskMarkedComplete();

    public abstract List<Task> tasks();

    public abstract boolean clearCompletedInProgress();

    public abstract boolean activateTaskInProgress();

    public abstract boolean completeTaskInProgress();

    public abstract boolean loadingIndicator();

    @Nullable
    public abstract String showTaskMarkedActiveFailed();

    public abstract boolean showLoadingTasksError();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder showSuccessfullySavedMessage(boolean showSuccessfullySavedMessage);

        public abstract Builder showAddTask(boolean showAddTask);

        public abstract Builder showTaskDetails(String showTaskDetails);

        public abstract Builder showCompletedTasksCleared(boolean showCompletedTasksCleared);

        public abstract Builder showTaskMarkedActive(boolean showTaskMarkedActive);

        public abstract Builder showTaskMarkedComplete(boolean showTaskMarkedComplete);

        public abstract Builder tasks(List<Task> tasks);

        public abstract Builder clearCompletedInProgress(boolean clearCompletedInProgress);

        public abstract Builder activateTaskInProgress(boolean activateTaskInProgress);

        public abstract Builder completeTaskInProgress(boolean completeTaskInProgress);

        public abstract Builder showTaskMarkedActiveFailed(String showTaskMarkedActiveFailed);

        public abstract Builder loadingIndicator(boolean loadingIndicator);

        public abstract Builder showLoadingTasksError(boolean showLoadingTasksError);

        public abstract ViewState build();

    }
}
