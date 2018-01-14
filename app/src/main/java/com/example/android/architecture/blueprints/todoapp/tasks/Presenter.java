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
public interface Presenter extends BasePresenter<View<ViewModel>> {

    void indicateTaskSaved();

    void loadTasks(FilterType filter);

    void refreshTasks(FilterType filtering);

    void addNewTask();

    void openTaskDetails(@NonNull Task requestedTask);

    void completeTask(@NonNull Task completedTask);

    void activateTask(@NonNull Task activeTask);

    void clearCompletedTasks();

}
