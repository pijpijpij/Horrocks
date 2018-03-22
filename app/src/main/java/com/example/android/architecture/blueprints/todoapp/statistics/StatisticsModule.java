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

package com.example.android.architecture.blueprints.todoapp.statistics;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped;
import com.example.android.architecture.blueprints.todoapp.statistics.presenter.FeaturedPresenter;
import com.example.android.architecture.blueprints.todoapp.statistics.ui.StatisticsFragment;
import com.pij.horrocks.DefaultEngine;
import com.pij.utils.Logger;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link FeaturedPresenter}.
 */
@Module
public abstract class StatisticsModule {

    @Provides
    static FeaturedPresenter provideStatisticsPresenter(TasksRepository repo, Logger logger) {
        return new FeaturedPresenter(repo, logger, new DefaultEngine<>(logger));
    }

    @FragmentScoped
    @ContributesAndroidInjector
    abstract StatisticsFragment statisticsFragment();

    @ActivityScoped
    @Binds
    abstract Presenter statitsticsPresenter(FeaturedPresenter presenter);
}
