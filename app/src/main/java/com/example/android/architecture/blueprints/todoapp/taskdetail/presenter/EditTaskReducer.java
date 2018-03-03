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

import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailModel;
import com.google.common.base.Strings;
import com.pij.horrocks.Reducer;

/**
 * <p>Created on 03/03/2018.</p>
 *
 * @author Pierrejean
 */
class EditTaskReducer implements Reducer<String, TaskDetailModel> {

    @Override
    public TaskDetailModel reduce(String taskId, TaskDetailModel state) {
        String actualId = Strings.isNullOrEmpty(taskId) ? null : taskId;
        return state.toBuilder().showEditTask(actualId).build();
    }
}
