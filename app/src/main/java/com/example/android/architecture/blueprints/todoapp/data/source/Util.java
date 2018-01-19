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

package com.example.android.architecture.blueprints.todoapp.data.source;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * <p>Created on 01/01/2018.</p>
 *
 * @author PierreJean
 */

public abstract class Util {
    @NonNull
    public static Single<List<Task>> loadTasksAsSingle(TasksDataSource dataSource) {
        return Single.create(emitter -> dataSource.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                emitter.onSuccess(tasks);
            }

            @Override
            public void onDataNotAvailable() {
                emitter.onError(new RuntimeException("No data available"));
            }
        }));
    }

    @NonNull
    public static Single<Task> loadTaskAsSingle(String taskId, TasksDataSource dataSource) {
        return Single.create(emitter -> dataSource.getTask(taskId, new TasksDataSource.GetTaskCallback() {
            @Override
            public void onTaskLoaded(Task task) {
                emitter.onSuccess(task);
            }

            @Override
            public void onDataNotAvailable() {
                emitter.onError(new RuntimeException("No data available"));
            }
        }));
    }

    @NonNull
    public static Completable saveTaskAsCompletable(String title, String description, TasksDataSource dataSource) {
        return saveTaskAsCompletable(new Task(title, description), dataSource);
    }

    @NonNull
    public static Completable updateTaskAsCompletable(String id, String title, String description, TasksDataSource dataSource) {
        return saveTaskAsCompletable(new Task(title, description, id), dataSource);
    }

    @NonNull
    public static Completable saveTaskAsCompletable(Task task, TasksDataSource dataSource) {
        return Completable.fromAction(() -> dataSource.saveTask(task));
    }

    @NonNull
    public static String errorMessage(@NonNull Throwable error) {
        return error.getMessage() == null ? "Unknown failure" : error.getMessage();
    }
}
