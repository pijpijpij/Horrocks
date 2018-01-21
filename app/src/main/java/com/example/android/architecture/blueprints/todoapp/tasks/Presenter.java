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

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.BasePresenter;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.pij.horrocks.View;

/**
 * <p>Created on 14/01/2018.</p>
 *
 * @author PierreJean
 */
public interface Presenter extends BasePresenter<View<TasksModel>> {

    void indicateTaskSaved();

    void loadTasks(FilterType filter);

    void refreshTasks(FilterType filtering);

    void addNewTask();

    void openTaskDetails(@NonNull Task requestedTask);

    void completeTask(@NonNull Task completedTask);

    void activateTask(@NonNull Task activeTask);

    void clearCompletedTasks();

}
