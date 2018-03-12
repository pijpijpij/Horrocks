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

import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailModel;

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
    public void emitsEditTask_whenTriggeredWithValidId() {
        EditTaskFeature sut = new EditTaskFeature();

        TaskDetailModel newState = sut.process("1").reduce(defaultState());

        assertThat(newState.showEditTask(), equalTo("1"));
    }

    @Test
    public void doesNotEmitsEditTask_whenTriggeredWithNullId() {
        EditTaskFeature sut = new EditTaskFeature();

        //noinspection ConstantConditions
        TaskDetailModel newState = sut.process(null).reduce(defaultState());

        assertThat(newState.showEditTask(), equalTo(null));
    }

    @Test
    public void doesNotEmitsEditTask_whenTriggeredWithEmptyId() {
        EditTaskFeature sut = new EditTaskFeature();

        TaskDetailModel newState = sut.process("").reduce(defaultState());

        assertThat(newState.showEditTask(), equalTo(null));
    }

}