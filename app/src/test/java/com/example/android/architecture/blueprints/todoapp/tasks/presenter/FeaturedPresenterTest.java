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
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource.LoadTasksCallback;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.tasks.FilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksModel;
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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
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
    private View<TasksModel> viewMock;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<LoadTasksCallback> mLoadTasksCallbackCaptor;
    @Captor
    private ArgumentCaptor<TasksModel> model;

    private FeaturedPresenter mTasksPresenter;

    @Before
    public void setupTasksPresenter() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);

        // Get a reference to the class under test
        mTasksPresenter = new FeaturedPresenter(mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        mTasksPresenter.takeView(viewMock);

        // We start the tasks to 3, with one active and two completed
        TASKS = asList(
                new Task("Title1", "Description1", "1"),
                new Task("Title2", "Description2", "2", true),
                new Task("Title3", "Description3", "3", true)
        );
    }

    @Test
    public void loadAllTasksFromRepositoryAndLoadIntoView() {
        reset(viewMock);
        // Given an initialized FeaturedPresenter with initialized tasks
        // When loading of Tasks is requested
        mTasksPresenter.refreshTasks(FilterType.ALL_TASKS);

        // Callback is captured and invoked with stubbed tasks twice
        //First time is when the fragment is bound to the view and a second time when we force another load
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        verify(viewMock, times(2)).display(model.capture());
        // Then progress indicator is shown
        // Then progress indicator is hidden and all tasks are shown in UI
        assertThat(model.getAllValues().stream().map(TasksModel::inProgress).collect(toList()), contains(true, false));
        assertThat(model.getValue().tasks(), hasSize(3));
    }

    @Test
    public void loadActiveTasksFromRepositoryAndLoadIntoView() {
        reset(viewMock);
        // Given an initialized FeaturedPresenter with initialized tasks
        // When loading of Tasks is requested
        mTasksPresenter.refreshTasks(FilterType.ACTIVE_TASKS);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        // Then progress indicator is hidden and active tasks are shown in UI
        verify(viewMock, times(2)).display(model.capture());
        assertThat(model.getAllValues().stream().map(TasksModel::inProgress).collect(toList()), contains(true, false));
        assertThat(model.getValue().tasks(), hasSize(1));
    }

    @Test
    public void loadCompletedTasksFromRepositoryAndLoadIntoView() {
        reset(viewMock);
        // Given an initialized FeaturedPresenter with initialized tasks
        // When loading of Tasks is requested
        mTasksPresenter.refreshTasks(FilterType.COMPLETED_TASKS);

        // Callback is captured and invoked with stubbed tasks
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        // Then progress indicator is hidden and completed tasks are shown in UI
        verify(viewMock, times(2)).display(model.capture());
        assertThat(model.getAllValues().stream().map(TasksModel::inProgress).collect(toList()), contains(true, false));
        assertThat(model.getValue().tasks(), hasSize(2));
    }

    @Test
    public void clickOnFab_ShowsAddTaskUi() {
        reset(viewMock);
        // When adding a new task
        mTasksPresenter.addNewTask();

        // Then add task UI is shown
        verify(viewMock).display(model.capture());
        assertThat(model.getValue().showAddTask(), is(true));
    }

    @Test
    public void clickOnTask_ShowsDetailUi() {
        reset(viewMock);
        // Given a stubbed active task
        Task requestedTask = new Task("Details Requested", "For this task");

        // When open task details is requested
        mTasksPresenter.openTaskDetails(requestedTask);

        // Then task detail UI is shown
        verify(viewMock).display(model.capture());
        assertNotNull(model.getValue().showTaskDetails());
    }

    @Test
    public void completeTask_ShowsTaskMarkedComplete() {
        reset(viewMock);
        // Given a stubbed task
        Task task = new Task("Details Requested", "For this task");

        // When task is marked as complete
        mTasksPresenter.completeTask(task);

        // Then repository is called and task marked complete UI is shown
        verify(mTasksRepository).completeTask(task);
    }

    @Test
    public void activateTask_ShowsTaskMarkedActive() {
        reset(viewMock);

        // Given a stubbed completed task
        Task task = new Task("Details Requested", "For this task", true);

        // When task is marked as activated
        mTasksPresenter.activateTask(task);
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onTasksLoaded(TASKS);

        // Then repository is called and task marked active UI is shown
        verify(mTasksRepository).activateTask(task);
        verify(viewMock, times(2)).display(model.capture());
        assertTrue(model.getValue().showTaskMarkedActive());
    }

    @Test
    public void unavailableTasks_ShowsError() {
        reset(viewMock);
        // When tasks are loaded
        mTasksPresenter.refreshTasks(FilterType.ALL_TASKS);

        // And the tasks aren't available in the repository
        verify(mTasksRepository).getTasks(mLoadTasksCallbackCaptor.capture());
        mLoadTasksCallbackCaptor.getValue().onDataNotAvailable();

        // Then an error message is shown
        verify(viewMock, times(2)).display(model.capture());
        assertTrue(model.getValue().showLoadingTasksError());
    }
}
