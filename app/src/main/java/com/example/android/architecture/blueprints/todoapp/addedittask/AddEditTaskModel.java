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

package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

/**
 * This specifies the contract between the presenter and the view.
 */
@AutoValue
public abstract class AddEditTaskModel {

    public static Builder builder() {
        return new AutoValue_AddEditTaskModel.Builder();
    }

    public abstract boolean showEmptyTaskError();

    public abstract boolean showTasksList();

    @Nullable
    public abstract String showTitle();

    @Nullable
    public abstract String showDescription();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder showEmptyTaskError(boolean showEmptyTaskError);

        public abstract Builder showTasksList(boolean showTasksList);

        public abstract Builder showTitle(String title);

        public abstract Builder showDescription(String description);

        public abstract AddEditTaskModel build();
    }

}
