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

package com.example.android.architecture.blueprints.todoapp.statistics.presenter;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSourceHelper;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsModel;
import com.pij.utils.SysoutLogger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.reactivex.observers.TestObserver;

import static com.example.android.architecture.blueprints.todoapp.statistics.presenter.StaticticsModelTextUtil.defaultState;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public class LoadStatisticsFeatureTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private TasksDataSource tasksRepositoryMock;
    private TasksDataSourceHelper tasksRepositoryHelper;

    private LoadStatisticsFeature sut;
    private Task completedTask;
    private Task activeTask;

    @Before
    public void setUp() {
        completedTask = new Task("zip", "zip", "zap", true);
        activeTask = new Task("zap", "zap", "zap", false);
        tasksRepositoryHelper = new TasksDataSourceHelper(tasksRepositoryMock);
        sut = new LoadStatisticsFeature(new SysoutLogger(), tasksRepositoryMock);
    }

    @Test
    public void emitsStartReducer_beforeRepositorySucceeds() {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<StatisticsModel> observer = sut.process(new Object()).map(reducer -> reducer.reduce(defaultState())).test();

        //noinspection Convert2MethodRef
        observer.assertValue(state -> state.progressIndicator());
    }

    @Test
    public void emitsStartAndSuccessReducers_whenRepositorySucceeds() {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<StatisticsModel> observer = sut.process(new Object()).map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.completeGetTasks(asList(activeTask, completedTask));

        observer.assertValueAt(1, state -> !state.progressIndicator());
        assertThat(observer.values().get(1).showStatistics(), equalTo(StatisticsModel.Numbers.create(1, 1)));
    }

    @Test
    public void completes_whenRepositorySucceeds() {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<StatisticsModel> observer = sut.process(new Object()).map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.completeGetTasks(asList(activeTask, completedTask));

        observer.assertComplete();
    }

    @Test
    public void emitsStartAndFailureReducers_whenRepositoryFails() {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<StatisticsModel> observer = sut.process(new Object()).map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.failGetTasks();

        observer.assertValueAt(1, state -> !state.progressIndicator() && state.showLoadingStatisticsError());
    }

    @Test
    public void completes_whenRepositoryFails() {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<StatisticsModel> observer = sut.process(new Object()).map(reducer -> reducer.reduce(defaultState())).test();
        tasksRepositoryHelper.failGetTasks();

        observer.assertComplete();
    }

}