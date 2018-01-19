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
    public abstract String title();

    @Nullable
    public abstract String description();

    // Not sure it really is completed()
    public abstract boolean completed();

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

        public abstract Builder title(String showTitle);

        public abstract Builder description(String showDescription);

        public abstract Builder completed(boolean showCompletionStatus);

        public abstract Builder showEditTask(String showEditTask);

        public abstract Builder close(boolean close);

        public abstract Builder showTaskMarkedComplete(boolean showTaskMarkedComplete);

        public abstract Builder showTaskMarkedActive(boolean showTaskMarkedActive);

        public abstract Builder showMissingTask(boolean showMissingTask);

        public abstract ViewModel build();
    }
}
