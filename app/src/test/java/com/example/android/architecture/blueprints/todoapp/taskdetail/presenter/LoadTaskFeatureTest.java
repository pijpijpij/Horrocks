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

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSourceHelper;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailModel;
import com.pij.utils.SysoutLogger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Objects;

import io.reactivex.observers.TestObserver;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.presenter.TaskDetailModelTextUtil.defaultState;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public class LoadTaskFeatureTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private TasksDataSource tasksRepositoryMock;
    private TasksDataSourceHelper tasksRepositoryHelper;

    private LoadTaskFeature sut;
    private Task zipzapTask;

    @Before
    public void setUp() {
        zipzapTask = new Task("zip", "zap");
        tasksRepositoryHelper = new TasksDataSourceHelper(tasksRepositoryMock);
        sut = new LoadTaskFeature(new SysoutLogger(), tasksRepositoryMock);
    }

    @Test
    public void emitsStartReducer_beforeRepositorySucceeds() {
        tasksRepositoryHelper.setupGetTask();

        TestObserver<TaskDetailModel> observer = sut.process("1").map(reducer -> reducer.reduce(defaultState())).test();

        //noinspection Convert2MethodRef
        observer.assertValue(state -> state.loadingIndicator());
    }

    @Test
    public void emitsStartAndSuccessReducers_whenRepositorySucceeds() {
        tasksRepositoryHelper.setupGetTask();

        TestObserver<TaskDetailModel> observer = sut.process("1").map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.completeGetTask(zipzapTask);

        observer.assertValueAt(1, state -> !state.loadingIndicator()
                && Objects.equals(state.title(), "zip")
                && Objects.equals(state.description(), "zap"));
    }

    @Test
    public void completes_whenRepositorySucceeds() {
        tasksRepositoryHelper.setupGetTask();

        TestObserver<TaskDetailModel> observer = sut.process("1").map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.completeGetTask(zipzapTask);

        observer.assertComplete();
    }

    @Test
    public void emitsStartAndFailureReducers_whenRepositoryFails() {
        tasksRepositoryHelper.setupGetTask();

        TestObserver<TaskDetailModel> observer = sut.process("1").map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.failGetTask();

        observer.assertValueAt(1, state -> !state.loadingIndicator() && state.showMissingTask());
    }

    @Test
    public void completes_whenRepositoryFails() {
        tasksRepositoryHelper.setupGetTask();

        TestObserver<TaskDetailModel> observer = sut.process("1").map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.failGetTask();

        observer.assertComplete();
    }

}