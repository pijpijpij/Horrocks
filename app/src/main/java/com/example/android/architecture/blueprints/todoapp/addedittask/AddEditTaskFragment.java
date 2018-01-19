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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.architecture.blueprints.todoapp.R;
import com.example.android.architecture.blueprints.todoapp.di.ActivityScoped;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerFragment;

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
@ActivityScoped
public class AddEditTaskFragment extends DaggerFragment {

    public static final String ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID";

    @Inject
    Presenter presenter;

    @BindView(R.id.add_task_title)
    TextView title;
    @BindView(R.id.add_task_description)
    TextView description;
    @BindView(R.id.fab_edit_task_done)
    FloatingActionButton fab;

    @Inject
    public AddEditTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroyView() {
        presenter.dropView();
        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.addtask_frag, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        //Bind view to the presenter which will signal for the presenter to load the task.
        presenter.takeView(this::display);

        if (savedInstanceState == null) {
            presenter.populateTask();
        }
    }

    @OnClick(R.id.fab_edit_task_done)
    void saveTask() {
        presenter.saveTask(title.getText().toString(), description.getText().toString());
    }

    private void showEmptyTaskError() {
        if (isActive()) {
            Snackbar.make(title, getString(R.string.empty_task_message), Snackbar.LENGTH_LONG).show();
        }
    }

    private void showTasksList() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    private boolean isActive() {
        return isAdded();
    }

    private void display(@NonNull ViewModel model) {
        title.setText(model.title());
        description.setText(model.description());
        if (model.showEmptyTaskError()) showEmptyTaskError();
        if (model.showTasksList()) showTasksList();
    }
}
