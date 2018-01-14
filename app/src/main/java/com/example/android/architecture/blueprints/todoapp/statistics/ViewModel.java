package com.example.android.architecture.blueprints.todoapp.statistics;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

/**
 * @author PierreJean
 */

@AutoValue
public abstract class ViewModel {

    public static Builder builder() {
        return new AutoValue_ViewModel.Builder();
    }

    public abstract boolean progressIndicator();

    @Nullable
    public abstract Numbers showStatistics();

    public abstract boolean showLoadingStatisticsError();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder showLoadingStatisticsError(boolean showLoadingStatisticsError);

        public abstract Builder progressIndicator(boolean progressIndicator);

        public abstract Builder showStatistics(Numbers showStatistics);

        public abstract ViewModel build();
    }

    @AutoValue
    public abstract static class Numbers {

        public static Numbers create(int numberOfIncompleteTasks, int numberOfCompletedTasks) {
            return builder()
                    .numberOfIncompleteTasks(numberOfIncompleteTasks)
                    .numberOfCompletedTasks(numberOfCompletedTasks)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_ViewModel_Numbers.Builder();
        }

        public abstract int numberOfIncompleteTasks();

        public abstract int numberOfCompletedTasks();

        public abstract Builder toBuilder();

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder numberOfIncompleteTasks(int numberOfIncompleteTasks);

            public abstract Builder numberOfCompletedTasks(int numberOfCompletedTasks);

            public abstract Numbers build();
        }
    }
}
