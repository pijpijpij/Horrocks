package com.example.android.architecture.blueprints.todoapp.statistics;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped;
import com.example.android.architecture.blueprints.todoapp.statistics.presenter.FeaturedPresenter;
import com.example.android.architecture.blueprints.todoapp.statistics.ui.StatisticsFragment;
import com.pij.horrocks.DefaultEngine;
import com.pij.horrocks.Logger;

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
