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

package com.example.android.architecture.blueprints.todoapp.taskdetail.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailModel;
import com.google.common.base.Strings;
import com.pij.horrocks.Interaction;
import com.pij.horrocks.Reducer;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */
class EditTaskFeature implements Interaction<String, TaskDetailModel> {

    @NonNull
    @Override
    public Reducer<TaskDetailModel> process(@NonNull String taskId) {
        String actualId = Strings.isNullOrEmpty(taskId) ? null : taskId;
        return current -> current.toBuilder().showEditTask(actualId).build();
    }

}
