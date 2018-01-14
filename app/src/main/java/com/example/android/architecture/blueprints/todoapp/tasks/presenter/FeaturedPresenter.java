/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.tasks.FilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.Presenter;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksModule;
import com.example.android.architecture.blueprints.todoapp.tasks.ViewModel;
import com.example.android.architecture.blueprints.todoapp.tasks.ui.TasksFragment;
import com.pij.horrocks.Configuration;
import com.pij.horrocks.Engine;
import com.pij.horrocks.Feature;
import com.pij.horrocks.Logger;
import com.pij.horrocks.MultipleResultFeature;
import com.pij.horrocks.SingleResultFeature;
import com.pij.horrocks.View;

import io.reactivex.disposables.CompositeDisposable;

import static com.example.android.architecture.blueprints.todoapp.tasks.FilterType.ALL_TASKS;
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
    private final Engine<ViewState, ViewModel> engine;
    private final Feature<Object, ViewState> indicateTaskSaved;
    private final Feature<Object, ViewState> showAddTask;
    private final Feature<Task, ViewState> openTaskDetails;
    private final Feature<Object, ViewState> clearCompletedTasks;
    private final Feature<Task, ViewState> activateTask;
    private final Feature<Task, ViewState> completeTask;
    private final Feature<FilterType, ViewState> loadTasks;
    private final Configuration<ViewState, ViewModel> engineConfiguration;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    public FeaturedPresenter(TasksDataSource tasksRepository, Logger logger, Engine<ViewState, ViewModel> engine) {
        this.logger = logger;

        indicateTaskSaved = new SingleResultFeature<>(new IndicateTaskSavedFeature());
        showAddTask = new SingleResultFeature<>(new ShowAddTaskFeature());
        openTaskDetails = new SingleResultFeature<>(new OpenTaskDetailsFeature());
        clearCompletedTasks = new MultipleResultFeature<>(new ClearCompletedTasksFeature(logger, tasksRepository));
        activateTask = new MultipleResultFeature<>(new ActivateTaskFeature(logger, tasksRepository));
        completeTask = new MultipleResultFeature<>(new CompleteTaskFeature(logger, tasksRepository));
        loadTasks = new MultipleResultFeature<>(new LoadTasksFeature(logger, tasksRepository));
        this.engine = engine;
        engineConfiguration = Configuration.<ViewState, ViewModel>builder()
                .initialState(initialState())
                .transientResetter(this::resetTransientState)
                .stateToModel(this::convert)
                .features(asList(
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
    private ViewModel convert(@NonNull ViewState state) {
        return ViewModel.builder()
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
    public void takeView(View<ViewModel> view) {
        subscription.add(
                engine.runWith(engineConfiguration).subscribe(
                        view::display,
                        e -> logger.print(getClass(), "Terminal Damage!!!", e),
                        () -> logger.print(getClass(), "takeView completed!!!"))
        );

        loadTasks.trigger(ALL_TASKS);
    }

    @Override
    public void dropView() {
        subscription.clear();
    }

}
