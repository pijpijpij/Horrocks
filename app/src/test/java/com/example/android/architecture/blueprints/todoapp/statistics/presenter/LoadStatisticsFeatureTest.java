package com.example.android.architecture.blueprints.todoapp.statistics.presenter;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSourceHelper;
import com.example.android.architecture.blueprints.todoapp.statistics.ViewModel;
import com.pij.horrocks.SysoutLogger;

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
    public void emitsStartResult_beforeRepositorySucceeds() throws Exception {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewModel> observer = sut.apply(new Object()).map(result -> result.applyTo(defaultState())).test();

        //noinspection Convert2MethodRef
        observer.assertValue(state -> state.progressIndicator());
    }

    @Test
    public void emitsStartAndSuccessResults_whenRepositorySucceeds() throws Exception {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewModel> observer = sut.apply(new Object()).map(result -> result.applyTo(defaultState())).test();
        tasksRepositoryHelper.completeGetTasks(asList(activeTask, completedTask));

        observer.assertValueAt(1, state -> !state.progressIndicator());
        assertThat(observer.values().get(1).showStatistics(), equalTo(ViewModel.Numbers.create(1, 1)));
    }

    @Test
    public void completes_whenRepositorySucceeds() throws Exception {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewModel> observer = sut.apply(new Object()).map(result -> result.applyTo(defaultState())).test();
        tasksRepositoryHelper.completeGetTasks(asList(activeTask, completedTask));

        observer.assertComplete();
    }

    @Test
    public void emitsStartAndFailureResults_whenRepositoryFails() throws Exception {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewModel> observer = sut.apply(new Object()).map(result -> result.applyTo(defaultState())).test();
        tasksRepositoryHelper.failGetTasks();

        observer.assertValueAt(1, state -> !state.progressIndicator() && state.showLoadingStatisticsError());
    }

    @Test
    public void completes_whenRepositoryFails() throws Exception {
        tasksRepositoryHelper.setupGetTasks();

        TestObserver<ViewModel> observer = sut.apply(new Object()).map(result -> result.applyTo(defaultState())).test();
        tasksRepositoryHelper.failGetTasks();

        observer.assertComplete();
    }

}