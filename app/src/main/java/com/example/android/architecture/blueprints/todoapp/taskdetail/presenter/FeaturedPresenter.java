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
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.Presenter;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailModel;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailModule;
import com.example.android.architecture.blueprints.todoapp.taskdetail.ui.TaskDetailFragment;
import com.google.common.base.Strings;
import com.pij.horrocks.Configuration;
import com.pij.horrocks.Engine;
import com.pij.horrocks.Feature;
import com.pij.horrocks.Logger;
import com.pij.horrocks.MemoryStore;
import com.pij.horrocks.MultipleResultFeature;
import com.pij.horrocks.SimpleReducerFeature;
import com.pij.horrocks.View;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

import static java.util.Arrays.asList;

/**
 * Listens to user actions from the UI ({@link TaskDetailFragment}), retrieves the data and updates
 * the UI as required.
 * <p>
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the FeaturedPresenter (if it fails, it emits a compiler error). It uses
 * {@link TaskDetailModule} to do so.
 * <p>
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 */
public final class FeaturedPresenter implements Presenter {

    private final Logger logger;
    private final CompositeDisposable subscription = new CompositeDisposable();
    private final Engine<TaskDetailModel, TaskDetailModel> engine;
    private final Configuration<TaskDetailModel, TaskDetailModel> engineConfiguration;
    private final Feature<String, TaskDetailModel> loadTask;
    private final Feature<String, TaskDetailModel> editTask;
    private final Feature<String, TaskDetailModel> deleteTask;
    private final Feature<String, TaskDetailModel> completeTask;
    private final Feature<String, TaskDetailModel> activateTask;
    @NonNull
    private String mTaskId;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    public FeaturedPresenter(@Nullable String taskId,
                             TasksRepository tasksRepository,
                             Logger logger,
                             Engine<TaskDetailModel, TaskDetailModel> engine) {
        this.logger = logger;
        mTaskId = Strings.nullToEmpty(taskId);
        loadTask = new MultipleResultFeature<>(new LoadTaskFeature(logger, tasksRepository));
        editTask = new SimpleReducerFeature<>(new EditTaskReducer());
        deleteTask = new MultipleResultFeature<>(new DeleteTaskFeature(logger, tasksRepository));
        completeTask = new MultipleResultFeature<>(new CompleteTaskFeature(logger, tasksRepository));
        activateTask = new MultipleResultFeature<>(new ActivateTaskFeature(logger, tasksRepository));
        this.engine = engine;
        engineConfiguration = Configuration.<TaskDetailModel, TaskDetailModel>builder()
                .store(new MemoryStore<>(initialState()))
                .transientResetter(this::resetTransientState)
                .stateToModel(state -> state)
                .features(asList(loadTask, editTask, deleteTask, completeTask, activateTask))
                .build();
    }

    @NonNull
    private TaskDetailModel resetTransientState(@NonNull TaskDetailModel state) {
        return state.toBuilder()
                .showTaskMarkedComplete(false)
                .showTaskMarkedActive(false)
                .showEditTask(null)
                .close(false)
                .build();
    }

    @NonNull
    private TaskDetailModel initialState() {
        return TaskDetailModel.builder()
                .close(false)
                .showEditTask(null)
                .showTaskMarkedActive(false)
                .showTaskMarkedComplete(false)
                .showMissingTask(true)
                .description(null)
                .title(null)
                .completed(false)
                .loadingIndicator(false)
                .build();
    }


    @Override
    public void editTask() {
        editTask.trigger(mTaskId);
    }

    @Override
    public void deleteTask() {
        deleteTask.trigger(mTaskId);
    }

    @Override
    public void completeTask() {
        completeTask.trigger(mTaskId);
    }

    @Override
    public void activateTask() {
        activateTask.trigger(mTaskId);
    }

    @Override
    public void takeView(View<TaskDetailModel> taskDetailView) {
        subscription.add(
                engine.runWith(engineConfiguration).subscribe(
                        taskDetailView::display,
                        e -> logger.print(getClass(), "Terminal Damage!!!", e),
                        () -> logger.print(getClass(), "takeView completed!!!"))
        );

        loadTask.trigger(mTaskId);
    }

    @Override
    public void dropView() {
        subscription.clear();
    }

}
