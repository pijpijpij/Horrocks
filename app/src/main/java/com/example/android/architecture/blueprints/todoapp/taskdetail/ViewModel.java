package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

/**
 * <p>Created on 10/01/2018.</p>
 *
 * @author PierreJean
 */
@AutoValue
public abstract class ViewModel {

    public static Builder builder() {
        return new AutoValue_ViewModel.Builder();
    }

    public abstract boolean loadingIndicator();

    public abstract boolean showMissingTask();

    @Nullable
    public abstract String showTitle();

    @Nullable
    public abstract String showDescription();

    // Not sure it really is completed()
    public abstract boolean showCompletionStatus();

    // transient
    @Nullable
    public abstract String showEditTask();

    public abstract boolean close();

    // transient
    public abstract boolean showTaskMarkedComplete();

    // transient
    public abstract boolean showTaskMarkedActive();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder loadingIndicator(boolean loadingIndicator);

        public abstract Builder showMissingTask(boolean showMissingTask);

        public abstract Builder showTitle(String showTitle);

        public abstract Builder showDescription(String showDescription);

        public abstract Builder showCompletionStatus(boolean showCompletionStatus);

        public abstract Builder showEditTask(String showEditTask);

        public abstract Builder close(boolean close);

        public abstract Builder showTaskMarkedComplete(boolean showTaskMarkedComplete);

        public abstract Builder showTaskMarkedActive(boolean showTaskMarkedActive);

        public abstract ViewModel build();
    }
}
