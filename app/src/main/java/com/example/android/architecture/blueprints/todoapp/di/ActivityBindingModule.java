package com.example.android.architecture.blueprints.todoapp.di;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskModule;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsModule;
import com.example.android.architecture.blueprints.todoapp.statistics.ui.StatisticsActivity;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailModule;
import com.example.android.architecture.blueprints.todoapp.taskdetail.ui.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.tasks.TasksModule;
import com.example.android.architecture.blueprints.todoapp.tasks.ui.TasksActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * We want Dagger.Android to create a Subcomponent which has a parent Component of whichever module ActivityBindingModule is on,
 * in our case that will be AppComponent. The beautiful part about this setup is that you never need to tell AppComponent that it is going to have all these subcomponents
 * nor do you need to tell these subcomponents that AppComponent exists.
 * We are also telling Dagger.Android that this generated SubComponent needs to include the specified modules and be aware of a scope annotation @ActivityScoped
 * When Dagger.Android annotation processor runs it will create 4 subcomponents for us.
 */
@Module
abstract class ActivityBindingModule {
    @ActivityScoped
    @ContributesAndroidInjector(modules = TasksModule.class)
    abstract TasksActivity tasksActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = AddEditTaskModule.class)
    abstract AddEditTaskActivity addEditTaskActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = StatisticsModule.class)
    abstract StatisticsActivity statisticsActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = TaskDetailModule.class)
    abstract TaskDetailActivity taskDetailActivity();
}
