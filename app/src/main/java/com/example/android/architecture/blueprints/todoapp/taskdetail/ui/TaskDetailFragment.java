/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.example.android.architecture.blueprints.todoapp.taskdetail.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;
import com.example.android.architecture.blueprints.todoapp.taskdetail.Presenter;
import com.example.android.architecture.blueprints.todoapp.taskdetail.ViewModel;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

/**
 * Main UI for the task detail screen.
 */
@ActivityScoped
public class TaskDetailFragment extends DaggerFragment {

    private static final int REQUEST_EDIT_TASK = 1;
    @Inject
    String taskId;
    @Inject
    Presenter mPresenter;
    private TextView mDetailTitle;
    private TextView mDetailDescription;
    private CheckBox mDetailCompleteStatus;

    @Inject
    public TaskDetailFragment() {
    }


    @Override
    public void onDestroy() {
        mPresenter.dropView();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.taskdetail_frag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mDetailTitle = view.findViewById(R.id.task_detail_title);
        mDetailDescription = view.findViewById(R.id.task_detail_description);
        mDetailCompleteStatus = view.findViewById(R.id.task_detail_complete);

        // Set up floating action button
        FloatingActionButton fab = getActivity().findViewById(R.id.fab_edit_task);

        fab.setOnClickListener(v -> mPresenter.editTask());
        mPresenter.takeView(this::display);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                mPresenter.deleteTask();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu);
    }

    private void setLoadingIndicator(boolean active) {
        if (active) {
            mDetailTitle.setText("");
            mDetailDescription.setText(getString(R.string.loading));
        }
    }

    private void showDescription(@Nullable String description) {
        if (Strings.isNullOrEmpty(description)) {
            mDetailDescription.setVisibility(View.GONE);
        } else {
            mDetailDescription.setVisibility(View.VISIBLE);
            mDetailDescription.setText(description);
        }
    }

    private void showCompletionStatus(final boolean complete) {
        Preconditions.checkNotNull(mDetailCompleteStatus);

        mDetailCompleteStatus.setChecked(complete);
        mDetailCompleteStatus.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        mPresenter.completeTask();
                    } else {
                        mPresenter.activateTask();
                    }
                });
    }

    private void showEditTask(@NonNull String taskId) {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    private void close() {
        getActivity().finish();
    }

    public void showTaskMarkedComplete() {
        Snackbar.make(getView(), R.string.task_marked_complete, Snackbar.LENGTH_LONG).show();
    }

    private void showTaskMarkedActive() {
        Snackbar.make(getView(), R.string.task_marked_active, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EDIT_TASK) {
            // If the task was edited successfully, go back to the list.
            if (resultCode == Activity.RESULT_OK) {
                getActivity().finish();
            }
        }
    }

    private void showTitle(@Nullable String title) {
        if (Strings.isNullOrEmpty(title)) {
            mDetailTitle.setVisibility(View.GONE);
        } else {
            mDetailTitle.setVisibility(View.VISIBLE);
            mDetailTitle.setText(title);
        }
    }

    private void showMissingTask() {
        mDetailTitle.setText("");
        mDetailDescription.setText(getString(R.string.no_data));
    }

    private void display(@NonNull ViewModel model) {
        if (model.showMissingTask()) showMissingTask();
        if (model.close()) close();
        setLoadingIndicator(model.loadingIndicator());
        showCompletionStatus(model.showCompletionStatus());
        String taskId = model.showEditTask();
        if (taskId != null) showEditTask(taskId);
        showTitle(model.showTitle());
        showDescription(model.showDescription());
        if (model.showTaskMarkedActive()) showTaskMarkedActive();
        if (model.showTaskMarkedComplete()) showTaskMarkedComplete();
    }
}
