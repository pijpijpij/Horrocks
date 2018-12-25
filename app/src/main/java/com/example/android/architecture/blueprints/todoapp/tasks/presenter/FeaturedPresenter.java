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

package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.tasks.FilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.Presenter;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksModel;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksModule;
import com.example.android.architecture.blueprints.todoapp.tasks.ui.TasksFragment;
import com.pij.horrocks.Configuration;
import com.pij.horrocks.Engine;
import com.pij.horrocks.MemoryStorage;
import com.pij.horrocks.MultipleReducerCreator;
import com.pij.horrocks.SingleReducerCreator;
import com.pij.horrocks.TriggeredReducerCreator;
import com.pij.horrocks.View;
import com.pij.utils.Logger;

import io.reactivex.disposables.CompositeDisposable;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;


/**
 * Listens to user actions from the UI ({@link TasksFragment}), retrieves the data and updates the
 * UI as required.
 * <p/>
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the FeaturedPresenter (if it fails, it emits a compiler error).  It uses
 * {@link TasksModule} to do so.
 * <p/>
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 **/
@ActivityScoped
public final class FeaturedPresenter implements Presenter {

    private final Logger logger;
    private final CompositeDisposable subscription = new CompositeDisposable();
    private final Engine<ViewState, TasksModel> engine;
    private final TriggeredReducerCreator<Object, ViewState> indicateTaskSaved;
    private final TriggeredReducerCreator<Object, ViewState> showAddTask;
    private final TriggeredReducerCreator<Task, ViewState> openTaskDetails;
    private final TriggeredReducerCreator<Object, ViewState> clearCompletedTasks;
    private final TriggeredReducerCreator<Task, ViewState> activateTask;
    private final TriggeredReducerCreator<Task, ViewState> completeTask;
    private final TriggeredReducerCreator<FilterType, ViewState> loadTasks;
    private final Configuration<ViewState, TasksModel> engineConfiguration;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    public FeaturedPresenter(TasksDataSource tasksRepository, Logger logger, Engine<ViewState, TasksModel> engine) {
        this.logger = logger;

        indicateTaskSaved = new SingleReducerCreator<>(new IndicateTaskSavedFeature(), logger);
        showAddTask = new SingleReducerCreator<>(new ShowAddTaskFeature(), logger);
        openTaskDetails = new SingleReducerCreator<>(new OpenTaskDetailsFeature(), logger);
        clearCompletedTasks = new MultipleReducerCreator<>(new ClearCompletedTasksFeature(logger, tasksRepository), logger);
        activateTask = new MultipleReducerCreator<>(new ActivateTaskFeature(logger, tasksRepository), logger);
        completeTask = new MultipleReducerCreator<>(new CompleteTaskFeature(logger, tasksRepository), logger);
        loadTasks = new MultipleReducerCreator<>(new LoadTasksFeature(logger, tasksRepository), logger);
        this.engine = engine;
        engineConfiguration = Configuration.<ViewState, TasksModel>builder()
                .store(new MemoryStorage<>(initialState()))
                .transientResetter(this::resetTransientState)
                .stateToModel(this::convert)
                .creators(asList(
                        indicateTaskSaved,
                        showAddTask,
                        openTaskDetails,
                        clearCompletedTasks,
                        activateTask,
                        completeTask,
                        loadTasks
                ))
                .build();
    }

    @NonNull
    private TasksModel convert(@NonNull ViewState state) {
        return TasksModel.builder()
                .tasks(state.tasks())
                .showTaskMarkedComplete(state.showTaskMarkedComplete())
                .showTaskMarkedActive(state.showTaskMarkedActive())
                .showAddTask(state.showAddTask())
                .showSuccessfullySavedMessage(state.showSuccessfullySavedMessage())
                .showTaskDetails(state.showTaskDetails())
                .showCompletedTasksCleared(state.showCompletedTasksCleared())
                .showLoadingTasksError(state.showLoadingTasksError())
                .inProgress(state.clearCompletedInProgress() || state.activateTaskInProgress() || state.completeTaskInProgress() ||
                        state.loadingIndicator())
                .build();
    }

    @NonNull
    private ViewState resetTransientState(@NonNull ViewState state) {
        return state.toBuilder()
                .showCompletedTasksCleared(false)
                .showTaskMarkedActive(false)
                .showTaskMarkedComplete(false)
                .showTaskDetails(null)
                .showSuccessfullySavedMessage(false)
                .showAddTask(false)
                .showTaskMarkedActiveFailed(null)
                .showLoadingTasksError(false)
                .build();
    }

    @NonNull
    private ViewState initialState() {
        return ViewState.builder()
                .showSuccessfullySavedMessage(false)
                .showAddTask(false)
                .showTaskDetails(null)
                .showCompletedTasksCleared(false)
                .showTaskMarkedActive(false)
                .showTaskMarkedComplete(false)
                .tasks(emptyList())
                .clearCompletedInProgress(false)
                .activateTaskInProgress(false)
                .completeTaskInProgress(false)
                .loadingIndicator(false)
                .showTaskMarkedActiveFailed(null)
                .showLoadingTasksError(false)
                .build();
    }

    @Override
    public void indicateTaskSaved() {
        indicateTaskSaved.trigger(new Object());
    }

    @Override
    public void loadTasks(FilterType filter) {
        loadTasks.trigger(filter);
    }

    @Override
    public void refreshTasks(FilterType filter) {
        loadTasks.trigger(filter);
    }

    @Override
    public void addNewTask() {
        showAddTask.trigger(new Object());
    }

    @Override
    public void openTaskDetails(@NonNull Task requestedTask) {
        openTaskDetails.trigger(requestedTask);
    }

    @Override
    public void completeTask(@NonNull Task completedTask) {
        completeTask.trigger(completedTask);
    }

    @Override
    public void activateTask(@NonNull Task activeTask) {
        activateTask.trigger(activeTask);
    }

    @Override
    public void clearCompletedTasks() {
        clearCompletedTasks.trigger(new Object());
    }

    @Override
    public void takeView(View<TasksModel> view) {
        subscription.add(
                engine.runWith(engineConfiguration).subscribe(
                        view::display,
                        e -> logger.print(getClass(), "Terminal Damage!!!", e),
                        () -> logger.print(getClass(), "takeView completed!!!"))
        );
    }

    @Override
    public void dropView() {
        subscription.clear();
    }

}
