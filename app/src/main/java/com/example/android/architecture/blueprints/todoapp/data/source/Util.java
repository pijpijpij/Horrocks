package com.example.android.architecture.blueprints.todoapp.data.source;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.List;

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
    public static String errorMessage(@NonNull Throwable error) {
        return error.getMessage() == null ? "Unknown failure" : error.getMessage();
    }
}
