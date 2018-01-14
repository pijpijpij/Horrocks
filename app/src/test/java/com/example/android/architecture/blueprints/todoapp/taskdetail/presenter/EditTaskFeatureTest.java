package com.example.android.architecture.blueprints.todoapp.taskdetail.presenter;

import com.example.android.architecture.blueprints.todoapp.taskdetail.ViewModel;

import org.junit.Test;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.presenter.TaskDetailModelTextUtil.defaultState;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * <p>Created on 02/01/2018.</p>
 *
 * @author PierreJean
 */
public class EditTaskFeatureTest {

    @Test
    public void emitsEditTask_whenTriggeredWithValidId() throws Exception {
        EditTaskFeature sut = new EditTaskFeature();

        ViewModel newState = sut.apply("1").applyTo(defaultState());

        assertThat(newState.showEditTask(), equalTo("1"));
    }

    @Test
    public void doesNotEmitsEditTask_whenTriggeredWithNullId() throws Exception {
        EditTaskFeature sut = new EditTaskFeature();

        ViewModel newState = sut.apply(null).applyTo(defaultState());

        assertThat(newState.showEditTask(), equalTo(null));
    }

    @Test
    public void doesNotEmitsEditTask_whenTriggeredWithEmptyId() throws Exception {
        EditTaskFeature sut = new EditTaskFeature();

        ViewModel newState = sut.apply("").applyTo(defaultState());

        assertThat(newState.showEditTask(), equalTo(null));
    }

}