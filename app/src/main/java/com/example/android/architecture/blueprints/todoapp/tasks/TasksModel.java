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
public abstract class TasksModel {

    public static Builder builder() {
        return new AutoValue_TasksModel.Builder();
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

        public abstract TasksModel build();

    }
}
