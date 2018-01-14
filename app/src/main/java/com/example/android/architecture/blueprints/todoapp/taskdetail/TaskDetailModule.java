package com.example.android.architecture.blueprints.todoapp.taskdetail;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.di.FragmentScoped;
import com.example.android.architecture.blueprints.todoapp.taskdetail.presenter.FeaturedPresenter;
import com.example.android.architecture.blueprints.todoapp.taskdetail.ui.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.taskdetail.ui.TaskDetailFragment;
import com.pij.horrocks.DefaultEngine;
import com.pij.horrocks.Logger;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

import static com.example.android.architecture.blueprints.todoapp.taskdetail.ui.TaskDetailActivity.EXTRA_TASK_ID;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link FeaturedPresenter}.
 */
@Module
public abstract class TaskDetailModule {

    @Provides
    static Presenter provideTaskDetailPresenter(String taskId, TasksRepository repo, Logger logger) {
        return new FeaturedPresenter(taskId, repo, logger, new DefaultEngine<>(logger));
    }

    @Provides
    @ActivityScoped
    static String provideTaskId(TaskDetailActivity activity) {
        return activity.getIntent().getStringExtra(EXTRA_TASK_ID);
    }

    @FragmentScoped
    @ContributesAndroidInjector
    abstract TaskDetailFragment taskDetailFragment();

}
