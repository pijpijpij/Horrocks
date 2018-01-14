/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.tasks.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.taskdetail.ui.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.tasks.FilterType;
import com.example.android.architecture.blueprints.todoapp.tasks.Presenter;
import com.example.android.architecture.blueprints.todoapp.tasks.ViewModel;

import java.util.List;

import javax.inject.Inject;

import activitystarter.ActivityStarter;
import activitystarter.Arg;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.DaggerFragment;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

/**
 * Display a grid of {@link Task}s. User can choose to view all, active or completed tasks.
 */
@ActivityScoped
public class TasksFragment extends DaggerFragment {

    @Inject
    Presenter presenter;
    /**
     * Listener for clicks on tasks in the ListView.
     */
    TaskItemListener itemListener = new TaskItemListener() {
        @Override
        public void onTaskClick(Task clickedTask) {
            presenter.openTaskDetails(clickedTask);
        }

        @Override
        public void onCompleteTaskClick(Task completedTask) {
            presenter.completeTask(completedTask);
        }

        @Override
        public void onActivateTaskClick(Task activatedTask) {
            presenter.activateTask(activatedTask);
        }
    };

    @BindView(R.id.refresh_layout)
    ScrollChildSwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tasks_list)
    ListView listView;
    @BindView(R.id.noTasks)
    View noTasksView;
    @BindView(R.id.noTasksIcon)
    ImageView noTaskIcon;
    @BindView(R.id.noTasksMain)
    TextView noTaskMainView;
    @BindView(R.id.noTasksAdd)
    TextView noTaskAddView;
    @BindView(R.id.tasksLL)
    LinearLayout tasksView;
    @BindView(R.id.filteringLabel)
    TextView filteringLabelView;
    @Arg(optional = true)
    FilterType filtering = FilterType.ALL_TASKS;
    private TasksAdapter adapter;
    private Unbinder unbinder;

    @Inject
    public TasksFragment() {
        // Requires empty public constructor
    }


    @Override
    public void onStop() {
        presenter.dropView();  //prevent leaking activity in case presenter is orchestrating a long running task
        super.onStop();
    }

    @Override
    public void onStart() {
        presenter.takeView(this::display);
        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//         call Presenter back.
        if (AddEditTaskActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            presenter.indicateTaskSaved();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tasks_frag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);

        // Set up tasks view
        adapter = new TasksAdapter(emptyList(), itemListener);
        listView.setAdapter(adapter);

        // Set up  no tasks view

        // Set up progress indicator
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(view.getContext(), R.color.colorPrimary),
                ContextCompat.getColor(view.getContext(), R.color.colorAccent),
                ContextCompat.getColor(view.getContext(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);
        swipeRefreshLayout.setOnRefreshListener(() -> presenter.loadTasks(filtering));

        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @OnClick({R.id.noTasksAdd, R.id.fab_add_task})
    void addNewTask() {
        presenter.addNewTask();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        ActivityStarter.fill(this, savedInstanceState);
        applyFilteringToUi(filtering);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ActivityStarter.save(this, outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                presenter.clearCompletedTasks();
                break;
            case R.id.menu_refresh:
                presenter.refreshTasks(filtering);
                break;
            case R.id.menu_active:
                setFiltering(FilterType.ACTIVE_TASKS);
                presenter.loadTasks(filtering);
                break;
            case R.id.menu_completed:
                setFiltering(FilterType.COMPLETED_TASKS);
                presenter.loadTasks(filtering);
                break;
            case R.id.menu_all:
                setFiltering(FilterType.ALL_TASKS);
                presenter.loadTasks(filtering);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu);
    }

    private void setFiltering(FilterType filtering) {
        this.filtering = filtering;
        applyFilteringToUi(filtering);
    }

    private void applyFilteringToUi(FilterType filter) {
        switch (filter) {
            case ACTIVE_TASKS:
                filteringLabelView.setText(R.string.label_active);
                break;
            case COMPLETED_TASKS:
                filteringLabelView.setText(R.string.label_completed);
                break;
            default:
                filteringLabelView.setText(R.string.label_all);
                break;
        }
        setupNoTaskViews(filter);
    }

    private void setLoadingIndicator(final boolean active) {
        if (getView() != null) {
            final SwipeRefreshLayout srl = getView().findViewById(R.id.refresh_layout);

            // Make sure setRefreshing() is called after the layout is done with everything else.
            srl.post(() -> srl.setRefreshing(active));
        }
    }

    private void showTasks(List<Task> tasks) {
        if (isActive()) {
            adapter.replaceData(tasks);

            if (tasks.isEmpty()) {
                tasksView.setVisibility(View.GONE);
                noTasksView.setVisibility(View.VISIBLE);
                noTaskAddView.setVisibility(View.VISIBLE);

            } else {
                tasksView.setVisibility(View.VISIBLE);
                noTasksView.setVisibility(View.GONE);
                noTaskAddView.setVisibility(View.GONE);
            }
        }
    }

    private void setupNoTaskViews(FilterType filtering) {
        int text;
        switch (filtering) {
            case ACTIVE_TASKS:
                text = R.string.no_tasks_active;
                break;
            case COMPLETED_TASKS:
                text = R.string.no_tasks_completed;
                break;
            default:
                text = R.string.no_tasks_all;
                break;
        }
        noTaskMainView.setText(text);
        int image;
        switch (filtering) {
            case ACTIVE_TASKS:
                image = R.drawable.ic_check_circle_24dp;
                break;
            case COMPLETED_TASKS:
                image = R.drawable.ic_verified_user_24dp;
                break;
            default:
                image = R.drawable.ic_assignment_turned_in_24dp;
                break;
        }
        noTaskIcon.setImageDrawable(getResources().getDrawable(image));
    }

    private void showSuccessfullySavedMessage() {
        // If a task was successfully added, show snackbar
        showMessage(getString(R.string.successfully_saved_task_message));
    }

    public void showAddTask() {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK);
    }

    private void showTaskDetailsUi(String taskId) {
        //Shown in it's own Activity, since it makes more sense that way
        // and it gives us the flexibility to show some Intent stubbing.
        Intent intent = new Intent(getContext(), TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }

    private void showTaskMarkedComplete() {
        showMessage(getString(R.string.task_marked_complete));
    }

    private void showTaskMarkedActive() {
        showMessage(getString(R.string.task_marked_active));
    }

    private void showCompletedTasksCleared() {
        showMessage(getString(R.string.completed_tasks_cleared));
    }

    private void showLoadingTasksError() {
        if (isActive()) {
            showMessage(getString(R.string.loading_tasks_error));
        }
    }

    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    private boolean isActive() {
        return isAdded();
    }

    private void display(@NonNull ViewModel model) {
        if (model.showSuccessfullySavedMessage()) showSuccessfullySavedMessage();
        if (model.showAddTask()) showAddTask();
        if (model.showTaskDetails() != null) showTaskDetailsUi(model.showTaskDetails());
        if (model.showCompletedTasksCleared()) showCompletedTasksCleared();
        if (model.showTaskMarkedActive()) showTaskMarkedActive();
        if (model.showTaskMarkedComplete()) showTaskMarkedComplete();
        setLoadingIndicator(model.inProgress());
        showTasks(model.tasks());
        if (model.showLoadingTasksError()) showLoadingTasksError();
    }


    public interface TaskItemListener {

        void onTaskClick(Task clickedTask);

        void onCompleteTaskClick(Task completedTask);

        void onActivateTaskClick(Task activatedTask);
    }

    private static class TasksAdapter extends BaseAdapter {

        private List<Task> tasks;
        private TaskItemListener itemListener;

        TasksAdapter(List<Task> tasks, TaskItemListener itemListener) {
            setList(tasks);
            this.itemListener = itemListener;
        }

        void replaceData(List<Task> tasks) {
            setList(tasks);
            notifyDataSetChanged();
        }

        private void setList(List<Task> tasks) {
            this.tasks = checkNotNull(tasks);
        }

        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public Task getItem(int i) {
            return tasks.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                rowView = inflater.inflate(R.layout.task_item, viewGroup, false);
            }

            final Task task = getItem(i);

            TextView titleTV = rowView.findViewById(R.id.title);
            titleTV.setText(task.getTitleForList());

            CheckBox completeCB = rowView.findViewById(R.id.complete);

            // Active/completed task UI
            completeCB.setChecked(task.isCompleted());
            if (task.isCompleted()) {
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.list_completed_touch_feedback));
            } else {
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.touch_feedback));
            }

            completeCB.setOnClickListener(v -> {
                if (!task.isCompleted()) {
                    itemListener.onCompleteTaskClick(task);
                } else {
                    itemListener.onActivateTaskClick(task);
                }
            });

            rowView.setOnClickListener(view1 -> itemListener.onTaskClick(task));

            return rowView;
        }
    }

}
