/*
 * Copyright 2018, Chiswick Forest
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

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

        public static Numbers create(int incompleteTasks, int completedTasks) {
            return builder()
                    .incompleteTasks(incompleteTasks)
                    .completedTasks(completedTasks)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_ViewModel_Numbers.Builder();
        }

        public abstract int incompleteTasks();

        public abstract int completedTasks();

        public abstract Builder toBuilder();

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder incompleteTasks(int incompleteTasks);

            public abstract Builder completedTasks(int completedTasks);

            public abstract Numbers build();
        }
    }
}
