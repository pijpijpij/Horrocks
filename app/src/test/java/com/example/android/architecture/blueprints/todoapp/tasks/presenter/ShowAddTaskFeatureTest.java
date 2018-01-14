package com.example.android.architecture.blueprints.todoapp.tasks.presenter;

import org.junit.Test;

import static com.example.android.architecture.blueprints.todoapp.tasks.presenter.TasksStateTextUtil.defaultState;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public class ShowAddTaskFeatureTest {

    @Test
    public void emitsShowAddTask_whenTriggered() throws Exception {
        ShowAddTaskFeature sut = new ShowAddTaskFeature();

        ViewState newState = sut.apply(new Object()).applyTo(defaultState());

        assertThat(newState.showAddTask(), equalTo(true));
    }

}