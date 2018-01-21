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
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsModel;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsModel.Numbers;
import com.google.common.collect.Lists;
import com.pij.horrocks.DefaultEngine;
import com.pij.horrocks.SysoutLogger;
import com.pij.horrocks.View;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link FeaturedPresenter}
 */
public class FeaturedPresenterTest {

    private static List<Task> TASKS;

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private View<StatisticsModel> mStatisticsView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TasksDataSource.LoadTasksCallback> mLoadTasksCallbackCaptor;
    @Captor
    private ArgumentCaptor<StatisticsModel> model;

    @Before
    public void setupStatisticsPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        FeaturedPresenter presenter = new FeaturedPresenter(mTasksRepository,
                new SysoutLogger(),
                new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mStatisticsView);

        // We start the tasks to 3, with one active and two completed
        TASKS = Lists.newArrayList(new Task("Title1", "Description1"),
                new Task("Title2", "Description2", true), new Task("Title3", "Description3", true));
    }

    @Test
    public void loadEmptyTasksFromRepository_CallViewToDisplay() {
        // Given an initialized FeaturedPresenter with no tasks

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(emptyList());

        // Then progress indicator is shown
        // Then progress indicator is hidden and correct data is passed on to the view
        verify(mStatisticsView, times(3)).display(model.capture());
        assertThat(model.getAllValues().stream().map(StatisticsModel::progressIndicator).collect(toList()), contains(false, true, false));
        assertThat(model.getValue().showStatistics(), equalTo(Numbers.create(0, 0)));
    }

    @Test
    public void loadNonEmptyTasksFromRepository_CallViewToDisplay() {
        // Given an initialized FeaturedPresenter with 1 active and 2 completed tasks


        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        // Then progress indicator is shown
        // Then progress indicator is hidden and correct data is passed on to the view
        verify(mStatisticsView, times(3)).display(model.capture());
        assertThat(model.getAllValues().stream().map(StatisticsModel::progressIndicator).collect(toList()), contains(false, true, false));
        assertThat(model.getValue().showStatistics(), equalTo(Numbers.create(1, 2)));
    }

    @Test
    public void loadStatisticsWhenTasksAreUnavailable_CallErrorToDisplay() {
        // And tasks data isn't available
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onDataNotAvailable();

        // Then an error message is shown
        verify(mStatisticsView, times(3)).display(model.capture());
        assertThat(model.getAllValues().stream().map(StatisticsModel::progressIndicator).collect(toList()), contains(false, true, false));
        assertTrue(model.getValue().showLoadingStatisticsError());
    }
}
