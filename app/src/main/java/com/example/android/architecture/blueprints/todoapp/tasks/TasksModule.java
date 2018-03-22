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

package com.example.android.architecture.blueprints.todoapp.tasks;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped;
import com.example.android.architecture.blueprints.todoapp.tasks.presenter.FeaturedPresenter;
import com.example.android.architecture.blueprints.todoapp.tasks.ui.TasksFragment;
import com.pij.horrocks.DefaultEngine;
import com.pij.utils.Logger;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the {@link Presenter}.
 */
@Module
public abstract class TasksModule {

    @ActivityScoped
    @Provides
    static Presenter provideTasksPresenter(TasksRepository repo, Logger logger) {
        return new FeaturedPresenter(repo, logger, new DefaultEngine<>(logger));
    }

    @FragmentScoped
    @ContributesAndroidInjector
    abstract TasksFragment tasksFragment();

}
