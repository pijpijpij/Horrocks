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

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSourceHelper;
import com.pij.horrocks.SysoutLogger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.reactivex.observers.TestObserver;

import static com.example.android.architecture.blueprints.todoapp.tasks.presenter.TasksStateTextUtil.defaultState;
import static java.util.Collections.singletonList;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public class ClearCompletedTasksFeatureTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private TasksDataSource tasksRepositoryMock;
    private TasksDataSourceHelper tasksRepositoryHelper;

    private ClearCompletedTasksFeature sut;
    private Task zipzapTask;

    @Before
    public void setUp() {
        zipzapTask = new Task("zip", "zap");
        tasksRepositoryHelper = new TasksDataSourceHelper(tasksRepositoryMock);
        sut = new ClearCompletedTasksFeature(new SysoutLogger(), tasksRepositoryMock);
    }

    @Test
    public void emitsStartReducer_beforeRepositorySucceeds() {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewState> observer = sut.process(zipzapTask).map(reducer -> reducer.reduce(defaultState())).test();

        observer.assertValue(state -> state.clearCompletedInProgress() && !state.showCompletedTasksCleared());
    }

    @Test
    public void emitsStartAndSuccessReducers_whenRepositorySucceeds() {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewState> observer = sut.process(zipzapTask).map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.completeGetTasks(singletonList(zipzapTask));

        observer.assertValueAt(1, state -> !state.clearCompletedInProgress() && state.showCompletedTasksCleared());
    }

    @Test
    public void completes_whenRepositorySucceeds() {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewState> observer = sut.process(zipzapTask).map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.completeGetTasks(singletonList(zipzapTask));

        observer.assertComplete();
    }

    @Test
    public void emitsStartAndFailureReducers_whenRepositoryFails() {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewState> observer = sut.process(zipzapTask).map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.failGetTasks();

        observer.assertValueAt(1, state -> !state.clearCompletedInProgress() && !state.showCompletedTasksCleared());
    }

    @Test
    public void completes_whenRepositoryFails() {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewState> observer = sut.process(zipzapTask).map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.failGetTasks();

        observer.assertComplete();
    }

}