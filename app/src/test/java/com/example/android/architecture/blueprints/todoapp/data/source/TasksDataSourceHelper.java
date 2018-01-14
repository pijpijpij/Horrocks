package com.example.android.architecture.blueprints.todoapp.data.source;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource.GetTaskCallback;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource.LoadTasksCallback;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public final class TasksDataSourceHelper {

    private final TasksDataSource tasksRepositoryMock;
    private final LoadTasksCallback[] loadTasksCallbacks = new LoadTasksCallback[1];
    private final GetTaskCallback[] getTaskCallbacks = new GetTaskCallback[1];

    public TasksDataSourceHelper(TasksDataSource tasksRepositoryMock) {
        this.tasksRepositoryMock = tasksRepositoryMock;
    }

    public void completeGetTasks(List<Task> response) {
        loadTasksCallbacks[0].onTasksLoaded(response);
    }

    public void failGetTasks() {
        loadTasksCallbacks[0].onDataNotAvailable();
    }

    public void setupGetTasks() {
        doAnswer(invocation -> loadTasksCallbacks[0] = (LoadTasksCallback) invocation.getArguments()[0])
                .when(tasksRepositoryMock).getTasks(any(LoadTasksCallback.class));
    }

    public void completeGetTask(Task response) {
        getTaskCallbacks[0].onTaskLoaded(response);
    }

    public void failGetTask() {
        getTaskCallbacks[0].onDataNotAvailable();
    }

    public void setupGetTask() {
        doAnswer(invocation -> getTaskCallbacks[0] = (GetTaskCallback) invocation.getArguments()[1])
                .when(tasksRepositoryMock).getTask(anyString(), any(GetTaskCallback.class));
    }

    public void setDeleteTaskSuccess(@SuppressWarnings("SameParameterValue") String taskId) {
//        doNothing().when(tasksRepositoryMock).deleteTask(taskId);
    }

    public void setDeleteTaskFailure(@SuppressWarnings("SameParameterValue") String taskId) {
        doThrow(new IllegalStateException("some dummy exception text")).when(tasksRepositoryMock).deleteTask(taskId);
    }

    public void setCompleteTaskSuccess(@SuppressWarnings("SameParameterValue") String taskId) {
//        doNothing().when(tasksRepositoryMock).completeTask(taskId);
    }

    public void setCompleteTaskFailure(@SuppressWarnings("SameParameterValue") String taskId) {
        doThrow(new IllegalStateException("some dummy exception text")).when(tasksRepositoryMock).completeTask(taskId);
    }

    public void setActivateTaskSuccess(@SuppressWarnings("SameParameterValue") String taskId) {
//        doNothing().when(tasksRepositoryMock).activateTask(taskId);
    }

    public void setActivateTaskFailure(@SuppressWarnings("SameParameterValue") String taskId) {
        doThrow(new IllegalStateException("some dummy exception text")).when(tasksRepositoryMock).activateTask(taskId);
    }
}