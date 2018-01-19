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
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.taskdetail.ViewModel;
import com.pij.horrocks.DefaultEngine;
import com.pij.horrocks.SysoutLogger;
import com.pij.horrocks.View;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link FeaturedPresenter}
 */
public class FeaturedPresenterTest {

    private static final String TITLE_TEST = "title";

    private static final String DESCRIPTION_TEST = "description";

    private static final String INVALID_TASK_ID = "";

    private static final Task ACTIVE_TASK = new Task(TITLE_TEST, DESCRIPTION_TEST);

    private static final Task COMPLETED_TASK = new Task(TITLE_TEST, DESCRIPTION_TEST, true);

    @Mock
    private TasksRepository mTasksRepository;

    @Mock
    private View<ViewModel> mTaskDetailView;

    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TasksDataSource.GetTaskCallback> mGetTaskCallbackCaptor;
    @Captor
    private ArgumentCaptor<ViewModel> model;

    private FeaturedPresenter presenter;

    @Before
    public void setup() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getActiveTaskFromRepositoryAndLoadIntoView() {
        // When tasks presenter is asked to open a task
        presenter = new FeaturedPresenter(
                ACTIVE_TASK.getId(), mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);

        // Then task is loaded from model, callback is captured and task is finally loaded
        verify(mTasksRepository).getTask(eq(ACTIVE_TASK.getId()), mGetTaskCallbackCaptor.capture());
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(ACTIVE_TASK); // Trigger callback

        // Then progress indicator is shown
        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        verify(mTaskDetailView, times(3)).display(model.capture());
        assertThat(model.getAllValues().stream().map(ViewModel::loadingIndicator).collect(toList()),
                contains(false, true, false));
        assertThat(model.getValue().title(), equalTo(TITLE_TEST));
        assertThat(model.getValue().description(), equalTo(DESCRIPTION_TEST));
        assertThat(model.getValue().completed(), equalTo(false));
    }

    @Test
    public void getCompletedTaskFromRepositoryAndLoadIntoView() {
        presenter = new FeaturedPresenter(
                COMPLETED_TASK.getId(), mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);

        // Then task is loaded from model, callback is captured and task is finally loaded
        verify(mTasksRepository).getTask(eq(COMPLETED_TASK.getId()), mGetTaskCallbackCaptor.capture());
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(COMPLETED_TASK); // Trigger callback

        // Then progress indicator is shown
        // Then progress indicator is hidden and title, description and completion status are shown
        // in UI
        verify(mTaskDetailView, times(3)).display(model.capture());
        assertThat(model.getAllValues().stream().map(ViewModel::loadingIndicator).collect(toList()),
                contains(false, true, false));
        assertThat(model.getValue().title(), equalTo(TITLE_TEST));
        assertThat(model.getValue().description(), equalTo(DESCRIPTION_TEST));
        assertThat(model.getValue().completed(), equalTo(true));
    }

    @Test
    public void getUnknownTaskFromRepositoryAndLoadIntoView() {
        // When loading of a task is requested with an invalid task ID.
        presenter = new FeaturedPresenter(
                INVALID_TASK_ID, mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);

        verify(mTaskDetailView, times(2)).display(model.capture());
        assertThat(model.getValue().showMissingTask(), equalTo(true));
    }

    @Test
    public void deleteTask_callsDatasource() {
        // Given an initialized FeaturedPresenter with stubbed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);

        // When the deletion of a task is requested
        presenter = new FeaturedPresenter(
                task.getId(), mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);
        // Then task is loaded from model, callback is captured and task is finally loaded
        verify(mTasksRepository).getTask(eq(task.getId()), mGetTaskCallbackCaptor.capture());
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(task); // Trigger callback

        presenter.deleteTask();

        // Then the repository is called
        verify(mTasksRepository).deleteTask(task.getId());
    }

    @Test
    public void deleteTask_closesView() {
        // Given an initialized FeaturedPresenter with stubbed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);

        // When the deletion of a task is requested
        presenter = new FeaturedPresenter(
                task.getId(), mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);
        // Then task is loaded from model, callback is captured and task is finally loaded
        verify(mTasksRepository).getTask(eq(task.getId()), mGetTaskCallbackCaptor.capture());
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(task); // Trigger callback

        presenter.deleteTask();

        // Then the view is notified
        verify(mTaskDetailView, times(5)).display(model.capture());
        assertThat(model.getValue().close(), equalTo(true));
    }

    @Test
    public void completeTask_callsDatasource() {
        // Given an initialized presenter with an active task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);
        presenter = new FeaturedPresenter(
                task.getId(), mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);
        verify(mTasksRepository).getTask(eq(task.getId()), mGetTaskCallbackCaptor.capture());
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(task); // Trigger callback

        // When the presenter is asked to complete the task
        presenter.completeTask();

        // Then a request is sent to the task repository
        verify(mTasksRepository).completeTask(task.getId());
    }

    @Test
    public void completeTask_showsTheTaskHasBeenMarkedComplete() {
        // Given an initialized presenter with an active task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST);
        presenter = new FeaturedPresenter(
                task.getId(), mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);
        verify(mTasksRepository).getTask(eq(task.getId()), mGetTaskCallbackCaptor.capture());
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(task); // Trigger callback

        // When the presenter is asked to complete the task
        presenter.completeTask();

        // Then the UI is updated
        verify(mTaskDetailView, times(5)).display(model.capture());
        assertThat(model.getValue().showTaskMarkedComplete(), equalTo(true));
    }

    @Test
    public void activateTask_callsDatasource() {
        // Given an initialized presenter with a completed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST, true);
        presenter = new FeaturedPresenter(
                task.getId(), mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);
        verify(mTasksRepository).getTask(eq(task.getId()), mGetTaskCallbackCaptor.capture());
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(task); // Trigger callback

        // When the presenter is asked to activate the task
        presenter.activateTask();

        // Then a request is sent to the task repository
        verify(mTasksRepository).activateTask(task.getId());
    }

    @Test
    public void activateTask_showTheTaskHasBeenMarkedActive() {
        // Given an initialized presenter with a completed task
        Task task = new Task(TITLE_TEST, DESCRIPTION_TEST, true);
        presenter = new FeaturedPresenter(
                task.getId(), mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);
        verify(mTasksRepository).getTask(eq(task.getId()), mGetTaskCallbackCaptor.capture());
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(task); // Trigger callback

        // When the presenter is asked to activate the task
        presenter.activateTask();

        // Then the UI is updated
        verify(mTaskDetailView, times(5)).display(model.capture());
        assertThat(model.getValue().showTaskMarkedActive(), equalTo(true));
    }

    @Test
    public void activeTaskIsShownWhenEditing() {
        // When the edit of an ACTIVE_TASK is requested
        presenter = new FeaturedPresenter(
                ACTIVE_TASK.getId(), mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);
        // Then task is loaded from model, callback is captured and task is finally loaded
        verify(mTasksRepository).getTask(eq(ACTIVE_TASK.getId()), mGetTaskCallbackCaptor.capture());
        mGetTaskCallbackCaptor.getValue().onTaskLoaded(COMPLETED_TASK); // Trigger callback

        presenter.editTask();

        // Then the view is notified
        verify(mTaskDetailView, times(4)).display(model.capture());
        assertThat(model.getValue().showEditTask(), equalTo(ACTIVE_TASK.getId()));
    }

    @Test
    public void invalidTaskIsNotShownWhenEditing() {
        // When the edit of an invalid task id is requested
        presenter = new FeaturedPresenter(
                INVALID_TASK_ID, mTasksRepository, new SysoutLogger(), new DefaultEngine<>(new SysoutLogger()));
        presenter.takeView(mTaskDetailView);

        presenter.editTask();

        // Then the edit mode is never started
        // instead, the error is shown. once when we try to open the task then again when we edit
        verify(mTaskDetailView, times(3)).display(model.capture());
        assertThat(model.getAllValues().stream().map(ViewModel::showEditTask).collect(toList()), contains((String) null, null, null));
        assertThat(model.getAllValues().stream().map(ViewModel::showMissingTask).collect(toList()), contains(true, true, true));
    }

}
