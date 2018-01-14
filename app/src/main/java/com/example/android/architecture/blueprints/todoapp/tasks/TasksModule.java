package com.example.android.architecture.blueprints.todoapp.tasks;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped;
import com.example.android.architecture.blueprints.todoapp.tasks.presenter.FeaturedPresenter;
import com.example.android.architecture.blueprints.todoapp.tasks.ui.TasksFragment;
import com.pij.horrocks.DefaultEngine;
import com.pij.horrocks.Logger;

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
