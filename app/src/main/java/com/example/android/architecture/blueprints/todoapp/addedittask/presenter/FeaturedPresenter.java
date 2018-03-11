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

package com.example.android.architecture.blueprints.todoapp.addedittask.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskModel;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskModule;
import com.example.android.architecture.blueprints.todoapp.addedittask.Presenter;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.google.common.base.Strings;
import com.pij.horrocks.Configuration;
import com.pij.horrocks.Engine;
import com.pij.horrocks.Logger;
import com.pij.horrocks.MemoryStorage;
import com.pij.horrocks.MultipleReducerCreator;
import com.pij.horrocks.ReducerCreator;
import com.pij.horrocks.View;

import javax.inject.Inject;

import dagger.Lazy;
import io.reactivex.disposables.CompositeDisposable;

import static java.util.Arrays.asList;

/**
 * Listens to user actions from the UI ({@link AddEditTaskFragment}), retrieves the data and
 * updates
 * the UI as required.
 * <p/>
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the FeaturedPresenter (if it fails, it emits a compiler error). It uses
 * {@link AddEditTaskModule} to do so.
 * <p/>
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually bypassing Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 */
public final class FeaturedPresenter implements Presenter {

    private final Logger logger;
    private final CompositeDisposable subscription = new CompositeDisposable();
    private final Engine<AddEditTaskModel, AddEditTaskModel> engine;
    private final Configuration<AddEditTaskModel, AddEditTaskModel> engineConfiguration;
    private final ReducerCreator<Task, AddEditTaskModel> saveTask;
    private final ReducerCreator<String, AddEditTaskModel> loadTask;

    private String taskId;

    // This is provided lazily because its value is determined in the Activity's onCreate. By
    // calling it in takeView(), the value is guaranteed to be set.
    private Lazy<Boolean> isDataMissingLazy;

    // Whether the data has been loaded with this presenter (or comes from a system restore)
    private boolean mIsDataMissing;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     *
     * @param taskId                 the task ID or null if it's a new task
     * @param tasksRepository        the data source
     * @param shouldLoadDataFromRepo a flag that controls whether we should load data from the
     *                               repository or not. It's lazy because it's determined in the
     *                               Activity's onCreate.
     */
    @Inject
    public FeaturedPresenter(@Nullable String taskId,
                             TasksDataSource tasksRepository,
                             Logger logger,
                             Engine<AddEditTaskModel, AddEditTaskModel> engine,
                             Lazy<Boolean> shouldLoadDataFromRepo) {
        this.logger = logger;
        this.taskId = Strings.nullToEmpty(taskId);
        isDataMissingLazy = shouldLoadDataFromRepo;
        saveTask = new MultipleReducerCreator<>(new SaveTaskFeature(logger, tasksRepository));
        loadTask = new MultipleReducerCreator<>(new LoadTaskFeature(logger, tasksRepository));
        this.engine = engine;
        engineConfiguration = Configuration.<AddEditTaskModel, AddEditTaskModel>builder()
                .store(new MemoryStorage<>(initialState()))
                .transientResetter(this::resetTransientState)
                .stateToModel(state -> state)
                .creators(asList(saveTask, loadTask))
                .build();
    }

    @NonNull
    private AddEditTaskModel resetTransientState(@NonNull AddEditTaskModel state) {
        return state.toBuilder()
                .showEmptyTaskError(false)
                .showTasksList(false)
                .showTitle(null)
                .showDescription(null)
                .build();
    }

    @NonNull
    private AddEditTaskModel initialState() {
        return AddEditTaskModel.builder()
                .showEmptyTaskError(false)
                .showTasksList(false)
                .showDescription("")
                .showTitle("")
                .build();
    }


    @Override
    public void saveTask(String title, String description) {
        Task task = taskId.isEmpty() ? new Task(title, description) : new Task(title, description, taskId);
        saveTask.trigger(task);
    }

    @Override
    public void populateTask() {
        loadTask.trigger(taskId);
    }

    @Override
    public void takeView(View<AddEditTaskModel> view) {
        subscription.add(
                engine.runWith(engineConfiguration).subscribe(
                        view::display,
                        e -> logger.print(getClass(), "Terminal Damage!!!", e),
                        () -> logger.print(getClass(), "takeView completed!!!"))
        );

//        // TODO migrate
//        mIsDataMissing = isDataMissingLazy.get();
//        if (!isNewTask() && mIsDataMissing) {
//            populateTask();
//        }
    }

    @Override
    public void dropView() {
        subscription.clear();
    }

    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }

    private boolean isNewTask() {
        return taskId.isEmpty();
    }

}
