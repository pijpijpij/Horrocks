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

import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.addedittask.presenter.FeaturedPresenter;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped;
import com.pij.horrocks.DefaultEngine;
import com.pij.horrocks.Logger;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

/**
 * This is a Dagger module. We use this to auto create the AdEditTaskSubComponent and bind
 * the {@link FeaturedPresenter} to the graph
 */
@Module
public abstract class AddEditTaskModule {

    // Rather than having the activity deal with getting the intent extra and passing it to the presenter
    // we will provide the taskId directly into the AddEditTaskActivitySubcomponent
    // which is what gets generated for us by Dagger.Android.
    // We can then inject our TaskId and state into our Presenter without having pass through dependency from
    // the Activity. Each UI object gets the dependency it needs and nothing else.
    @Provides
    @ActivityScoped
    @Nullable
    static String provideTaskId(AddEditTaskActivity activity) {
        return activity.getIntent().getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID);
    }

    @Provides
    @ActivityScoped
    static boolean provideStatusDataMissing(AddEditTaskActivity activity) {
        return activity.isDataMissing();
    }

    @ActivityScoped
    @Provides
    static Presenter provideAddEditTaskPresenter(@Nullable String taskId, TasksRepository repo, Logger logger, Lazy<Boolean> loadData) {
        return new FeaturedPresenter(taskId, repo, logger, new DefaultEngine<>(logger), loadData);
    }

    @FragmentScoped
    @ContributesAndroidInjector
    abstract AddEditTaskFragment addEditTaskFragment();

    //NOTE:  IF you want to have something be only in the Fragment scope but not activity mark a
    //@provides or @Binds method as @FragmentScoped.  Use case is when there are multiple fragments
    //in an activity but you do not want them to share all the same objects.
}
