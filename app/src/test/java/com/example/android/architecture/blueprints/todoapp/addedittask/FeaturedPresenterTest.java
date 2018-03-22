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

package com.example.android.architecture.blueprints.todoapp.addedittask;

import com.example.android.architecture.blueprints.todoapp.addedittask.presenter.FeaturedPresenter;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.pij.horrocks.DefaultEngine;
import com.pij.horrocks.View;
import com.pij.utils.SysoutLogger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dagger.Lazy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link FeaturedPresenter}.
 */
public class FeaturedPresenterTest {

    private final Lazy<Boolean> booleanLazy = () -> true;
    @Mock
    private TasksRepository dataSourceMock;
    @Mock
    private View<AddEditTaskModel> viewMock;
    /**
     * {@link ArgumentCaptor} is a powerful Mockito API to capture argument values and use them to
     * perform further actions or assertions on them.
     */
    @Captor
    private ArgumentCaptor<TasksDataSource.GetTaskCallback> getTaskCallback;
    @Captor
    private ArgumentCaptor<AddEditTaskModel> model;
    private FeaturedPresenter sut;

    @Before
    public void setupMocksAndView() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void saveNewTaskToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        sut = new FeaturedPresenter("1", dataSourceMock, new SysoutLogger(),
                new DefaultEngine<>(new SysoutLogger()),
                booleanLazy);
        sut.takeView(viewMock);
        // When the sut is asked to save a task
        sut.saveTask("New Task Title", "Some Task Description");

        // Then a task is saved in the repository and the viewMock updated
        verify(dataSourceMock).saveTask(any(Task.class)); // saved to the model
        verify(viewMock, times(3)).display(model.capture());
        assertThat(model.getValue().showTasksList(), is(true)); // shown in the UI
    }

    @Test
    public void saveTask_emptyTaskShowsErrorUi() {
        // Get a reference to the class under test
        sut = new FeaturedPresenter(null, dataSourceMock, new SysoutLogger(),
                new DefaultEngine<>(new SysoutLogger()),
                booleanLazy);
        sut.takeView(viewMock);

        // When the sut is asked to save an empty task
        sut.saveTask("", "");

        // Then an empty not error is shown in the UI
        verify(viewMock, times(2)).display(model.capture());
        assertThat(model.getValue().showEmptyTaskError(), is(true));
    }

    @Test
    public void saveExistingTaskToRepository_showsSuccessMessageUi() {
        // Get a reference to the class under test
        sut = new FeaturedPresenter("1", dataSourceMock, new SysoutLogger(),
                new DefaultEngine<>(new SysoutLogger()),
                booleanLazy);
        sut.takeView(viewMock);

        // When the sut is asked to save an existing task
        sut.saveTask("New Task Title", "Some Task Description");

        // Then a task is saved in the repository and the viewMock updated
        verify(dataSourceMock).saveTask(any(Task.class)); // saved to the model
        verify(viewMock, times(3)).display(model.capture());
        assertThat(model.getValue().showTasksList(), is(true)); // shown in the UI
    }

    @Test
    public void populateTask_callsRepoAndUpdatesView() {
        Task testTask = new Task("TITLE", "DESCRIPTION");
        // Get a reference to the class under test
        sut = new FeaturedPresenter(testTask.getId(), dataSourceMock, new SysoutLogger(),
                new DefaultEngine<>(new SysoutLogger()),
                booleanLazy);
        //When we bind the viewMock we will also populate the task
        sut.takeView(viewMock);

        sut.populateTask();

        // Then the task repository is queried and the viewMock updated
        verify(dataSourceMock).getTask(eq(testTask.getId()), getTaskCallback.capture());

        // Simulate callback
        getTaskCallback.getValue().onTaskLoaded(testTask);

        verify(viewMock, times(3)).display(model.capture());
        assertThat(model.getValue().showTitle(), is("TITLE"));
        assertThat(model.getValue().showDescription(), is("DESCRIPTION"));
    }
}
