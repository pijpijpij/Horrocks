package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import org.junit.Test;

import static com.example.android.architecture.blueprints.todoapp.tasks.presenter.TasksStateTextUtil.defaultState;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public class OpenTaskDetailsFeatureTest {

    @Test
    public void emitsShowTaskDetailsWithId_whenTriggered() throws Exception {
        OpenTaskDetailsFeature sut = new OpenTaskDetailsFeature();
        Task aTask = new Task("zip", "zap", "the id");

        ViewState newState = sut.apply(aTask).applyTo(defaultState());

        assertThat(newState.showTaskDetails(), equalTo("the id"));
    }

}