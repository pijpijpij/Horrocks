package com.example.android.architecture.blueprints.todoapp.taskdetail.presenter;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSourceHelper;
import com.example.android.architecture.blueprints.todoapp.taskdetail.ViewModel;
import com.pij.horrocks.SysoutLogger;

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
public class CompleteTaskFeatureTest {

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private TasksDataSource tasksRepositoryMock;
    private TasksDataSourceHelper tasksRepositoryHelper;

    private CompleteTaskFeature sut;
    private Task zipzapTask;

    @Before
    public void setUp() {
        zipzapTask = new Task("zip", "zap");
        tasksRepositoryHelper = new TasksDataSourceHelper(tasksRepositoryMock);
        sut = new CompleteTaskFeature(new SysoutLogger(), tasksRepositoryMock);
    }

    @Test
    public void emitsStartResult_beforeRepositorySucceeds() throws Exception {
        tasksRepositoryHelper.setCompleteTaskSuccess("1");

        TestObserver<ViewModel> observer = sut.apply("1").map(result -> result.applyTo(defaultState())).test();

        //noinspection Convert2MethodRef
        observer.assertValueAt(0, state -> state.loadingIndicator());
    }

    @Test
    public void emitsStartAndSuccessResults_whenRepositorySucceeds() throws Exception {
        tasksRepositoryHelper.setCompleteTaskSuccess("1");

        TestObserver<ViewModel> observer = sut.apply("1").map(result -> result.applyTo(defaultState())).test();

        observer.assertValueAt(1, state -> !state.loadingIndicator()
                && Objects.equals(state.showTaskMarkedComplete(), true));
    }

    @Test
    public void completes_whenRepositorySucceeds() throws Exception {
        tasksRepositoryHelper.setCompleteTaskSuccess("1");

        TestObserver<ViewModel> observer = sut.apply("1").map(result -> result.applyTo(defaultState())).test();

        observer.assertComplete();
    }

    @Test
    public void emitsStartAndFailureResults_whenRepositoryFails() throws Exception {
        tasksRepositoryHelper.setCompleteTaskFailure("1");

        TestObserver<ViewModel> observer = sut.apply("1").map(result -> result.applyTo(defaultState())).test();

        observer.assertValueAt(1, state -> !state.loadingIndicator() && !state.showTaskMarkedComplete());
    }

    @Test
    public void completes_whenRepositoryFails() throws Exception {
        tasksRepositoryHelper.setCompleteTaskSuccess("1");

        TestObserver<ViewModel> observer = sut.apply("1").map(result -> result.applyTo(defaultState())).test();

        observer.assertComplete();
    }

}