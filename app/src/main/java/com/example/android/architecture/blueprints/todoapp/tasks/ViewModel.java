package com.example.android.architecture.blueprints.todoapp.tasks;

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
public abstract class ViewModel {

    public static Builder builder() {
        return new AutoValue_ViewModel.Builder();
    }

    public abstract boolean showSuccessfullySavedMessage();

    public abstract boolean showAddTask();

    @Nullable
    public abstract String showTaskDetails();

    public abstract boolean showCompletedTasksCleared();

    public abstract boolean showTaskMarkedActive();

    public abstract boolean showTaskMarkedComplete();

    public abstract List<Task> tasks();

    public abstract boolean inProgress();

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

        public abstract Builder inProgress(boolean inProgress);

        public abstract Builder showLoadingTasksError(boolean showLoadingTasksError);

        public abstract ViewModel build();

    }
}
