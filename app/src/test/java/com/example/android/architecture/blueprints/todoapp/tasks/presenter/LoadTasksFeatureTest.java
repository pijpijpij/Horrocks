package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSourceHelper;
import com.example.android.architecture.blueprints.todoapp.tasks.FilterType;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public class LoadTasksFeatureTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private TasksDataSource tasksRepositoryMock;
    private TasksDataSourceHelper tasksRepositoryHelper;

    private LoadTasksFeature sut;
    private Task zipzapTask;

    @Before
    public void setUp() {
        zipzapTask = new Task("zip", "zap");
        tasksRepositoryHelper = new TasksDataSourceHelper(tasksRepositoryMock);
        sut = new LoadTasksFeature(new SysoutLogger(), tasksRepositoryMock);
    }

    @Test
    public void emitsStartResult_beforeRepositorySucceeds() throws Exception {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewState> observer = sut.apply(FilterType.ALL_TASKS).map(result -> result.applyTo(defaultState())).test();

        //noinspection Convert2MethodRef
        observer.assertValue(state -> state.loadingIndicator());
    }

    @Test
    public void emitsStartAndSuccessResults_whenRepositorySucceeds() throws Exception {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewState> observer = sut.apply(FilterType.ALL_TASKS).map(result -> result.applyTo(defaultState())).test();
        tasksRepositoryHelper.completeGetTasks(singletonList(zipzapTask));

        observer.assertValueAt(1, state -> !state.loadingIndicator());
        assertThat(observer.values().get(1).tasks(), containsInAnyOrder(zipzapTask));
    }

    @Test
    public void completes_whenRepositorySucceeds() throws Exception {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewState> observer = sut.apply(FilterType.ALL_TASKS).map(result -> result.applyTo(defaultState())).test();
        tasksRepositoryHelper.completeGetTasks(singletonList(zipzapTask));

        observer.assertComplete();
    }

    @Test
    public void emitsStartAndFailureResults_whenRepositoryFails() throws Exception {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewState> observer = sut.apply(FilterType.ALL_TASKS).map(result -> result.applyTo(defaultState())).test();
        tasksRepositoryHelper.failGetTasks();

        observer.assertValueAt(1, state -> !state.loadingIndicator() && state.showLoadingTasksError());
    }

    @Test
    public void completes_whenRepositoryFails() throws Exception {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewState> observer = sut.apply(FilterType.ALL_TASKS).map(result -> result.applyTo(defaultState())).test();
        tasksRepositoryHelper.failGetTasks();

        observer.assertComplete();
    }

}