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

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public abstract class TaskDetailModelTextUtil {

    public static TaskDetailModel defaultState() {
        return TaskDetailModel.builder()
                .loadingIndicator(false)
                .close(false)
                .completed(false)
                .title(null)
                .description(null)
                .showEditTask(null)
                .showMissingTask(false)
                .showTaskMarkedActive(false)
                .showTaskMarkedComplete(false)
                .build();
    }

}